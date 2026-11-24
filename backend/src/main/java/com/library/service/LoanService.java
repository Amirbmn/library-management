package com.library.service;

import com.library.dto.Dtos.LoanDto;
import com.library.exception.ApiException;
import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Locking order everywhere: Book -> User (or Loan -> Book -> User) to avoid deadlocks.
 * The Book row is locked with PESSIMISTIC_WRITE, so concurrent borrow requests for the
 * last copy are serialized and stock can never go negative.
 */
@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loans;
    private final BookRepository books;
    private final UserRepository users;
    private final ReservationRepository reservations;

    @Value("${library.loan-days:14}") private int loanDays;
    @Value("${library.max-active-loans:3}") private int maxActiveLoans;
    @Value("${library.fine-per-day:1.00}") private BigDecimal finePerDay;

    @Transactional
    public LoanDto borrow(Long userId, Long bookId) {
        Book book = books.findLockedById(bookId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found"));
        User user = users.findLockedById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        String problem = standingProblem(user);
        if (problem != null) throw new ApiException(HttpStatus.FORBIDDEN, problem);
        if (loans.countByUserAndStatus(user, LoanStatus.ACTIVE) >= maxActiveLoans)
            throw new ApiException(HttpStatus.CONFLICT,
                    "You already have " + maxActiveLoans + " active loans (maximum)");
        if (loans.existsByUserAndBookAndStatus(user, book, LoanStatus.ACTIVE))
            throw new ApiException(HttpStatus.CONFLICT, "You already have a copy of this book");
        if (book.getAvailableCopies() <= 0)
            throw new ApiException(HttpStatus.CONFLICT,
                    "No copies available. You can reserve this book instead.");

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return toDto(createLoan(user, book));
    }

    @Transactional
    public LoanDto giveBack(Long userId, Long loanId) {
        Loan loan = loans.findLockedById(loanId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Loan not found"));
        if (!loan.getUser().getId().equals(userId))
            throw new ApiException(HttpStatus.NOT_FOUND, "Loan not found");
        if (loan.getStatus() != LoanStatus.ACTIVE)
            throw new ApiException(HttpStatus.CONFLICT, "This book is already returned");

        Book book = books.findLockedById(loan.getBook().getId()).orElseThrow();
        LocalDate today = LocalDate.now();
        long late = Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), today));
        loan.setReturnDate(today);
        loan.setStatus(LoanStatus.RETURNED);
        if (late > 0) {
            loan.setFineAmount(finePerDay.multiply(BigDecimal.valueOf(late)));
            loan.setFinePaid(false);
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        fulfillQueue(book);
        return toDto(loan);
    }

    /**
     * Hands free copies to the waiting queue in order. Ineligible users get their
     * reservation cancelled and the next one is tried. Copies nobody takes stay available.
     * Must be called with the book row already locked.
     */
    public void fulfillQueue(Book book) {
        while (book.getAvailableCopies() > 0) {
            Reservation next = reservations
                    .findFirstByBookAndStatusOrderByIdAsc(book, ReservationStatus.WAITING).orElse(null);
            if (next == null) break;
            User user = users.findLockedById(next.getUser().getId()).orElse(null);
            boolean eligible = user != null
                    && standingProblem(user) == null
                    && loans.countByUserAndStatus(user, LoanStatus.ACTIVE) < maxActiveLoans
                    && !loans.existsByUserAndBookAndStatus(user, book, LoanStatus.ACTIVE);
            if (eligible) {
                createLoan(user, book);
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                next.setStatus(ReservationStatus.FULFILLED);
            } else {
                next.setStatus(ReservationStatus.CANCELLED);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<LoanDto> myLoans(Long userId) {
        User user = users.findById(userId).orElseThrow();
        return loans.findByUserOrderByIdDesc(user).stream().map(this::toDto).toList();
    }

    /** null when the account is in good standing */
    public String standingProblem(User user) {
        if (loans.existsByUserAndStatusAndDueDateBefore(user, LoanStatus.ACTIVE, LocalDate.now()))
            return "You have overdue books. Return them before borrowing or reserving.";
        if (!loans.findByUserAndStatusAndFinePaidFalse(user, LoanStatus.RETURNED).isEmpty())
            return "You have unpaid fines. Settle them before borrowing or reserving.";
        return null;
    }

    private Loan createLoan(User user, Book book) {
        Loan l = new Loan();
        l.setUser(user);
        l.setBook(book);
        l.setBorrowDate(LocalDate.now());
        l.setDueDate(LocalDate.now().plusDays(loanDays));
        l.setStatus(LoanStatus.ACTIVE);
        return loans.save(l);
    }

    public LoanDto toDto(Loan l) {
        LocalDate today = LocalDate.now();
        boolean active = l.getStatus() == LoanStatus.ACTIVE;
        boolean overdue = active && today.isAfter(l.getDueDate());
        long late;
        BigDecimal fine;
        if (overdue) {
            late = ChronoUnit.DAYS.between(l.getDueDate(), today);
            fine = finePerDay.multiply(BigDecimal.valueOf(late));
        } else if (!active && l.getReturnDate() != null) {
            late = Math.max(0, ChronoUnit.DAYS.between(l.getDueDate(), l.getReturnDate()));
            fine = l.getFineAmount();
        } else {
            late = 0;
            fine = BigDecimal.ZERO;
        }
        return new LoanDto(l.getId(), l.getBook().getId(), l.getBook().getTitle(), l.getBook().getAuthor(),
                l.getBorrowDate(), l.getDueDate(), l.getReturnDate(), l.getStatus().name(),
                overdue, late, fine, l.isFinePaid());
    }
}
