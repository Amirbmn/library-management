package com.library.service;

import com.library.dto.Dtos.ReservationDto;
import com.library.exception.ApiException;
import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservations;
    private final BookRepository books;
    private final UserRepository users;
    private final LoanRepository loans;
    private final LoanService loanService;

    @Transactional
    public ReservationDto reserve(Long userId, Long bookId) {
        Book book = books.findLockedById(bookId)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found"));
        User user = users.findLockedById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        String problem = loanService.standingProblem(user);
        if (problem != null) throw new ApiException(HttpStatus.FORBIDDEN, problem);
        if (book.getAvailableCopies() > 0)
            throw new ApiException(HttpStatus.CONFLICT, "Copies are available, you can borrow this book directly");
        if (loans.existsByUserAndBookAndStatus(user, book, LoanStatus.ACTIVE))
            throw new ApiException(HttpStatus.CONFLICT, "You already have this book on loan");
        if (reservations.existsByUserAndBookAndStatus(user, book, ReservationStatus.WAITING))
            throw new ApiException(HttpStatus.CONFLICT, "You are already in the queue for this book");

        Reservation r = new Reservation();
        r.setUser(user);
        r.setBook(book);
        r.setCreatedAt(LocalDateTime.now());
        r.setStatus(ReservationStatus.WAITING);
        reservations.save(r);
        return toDto(r);
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        Reservation r = reservations.findById(reservationId)
                .filter(x -> x.getUser().getId().equals(userId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found"));
        // lock the book so cancelling does not race with queue processing
        books.findLockedById(r.getBook().getId());
        Reservation fresh = reservations.findById(reservationId).orElseThrow();
        if (fresh.getStatus() != ReservationStatus.WAITING)
            throw new ApiException(HttpStatus.CONFLICT, "Reservation is no longer waiting");
        fresh.setStatus(ReservationStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> mine(Long userId) {
        User user = users.findById(userId).orElseThrow();
        return reservations.findByUserAndStatusOrderByIdAsc(user, ReservationStatus.WAITING)
                .stream().map(this::toDto).toList();
    }

    private ReservationDto toDto(Reservation r) {
        long position = reservations.countByBookAndStatusAndIdLessThan(r.getBook(), ReservationStatus.WAITING, r.getId()) + 1;
        return new ReservationDto(r.getId(), r.getBook().getId(), r.getBook().getTitle(),
                r.getBook().getAuthor(), position, r.getCreatedAt());
    }
}
