package com.library.service;

import com.library.dto.Dtos.*;
import com.library.exception.ApiException;
import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository books;
    private final LoanRepository loans;
    private final ReservationRepository reservations;
    private final LoanService loanService;

    @Transactional(readOnly = true)
    public PageResponse<BookDto> search(String search, String category, boolean availableOnly,
                                        int page, int size, Long userId) {
        String q = "%" + (search == null ? "" : search.trim().toLowerCase()) + "%";
        String cat = category == null ? "" : category.trim();
        Page<Book> p = books.search(q, cat, availableOnly,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("title")));
        List<Long> ids = p.getContent().stream().map(Book::getId).toList();
        Set<Long> borrowed = ids.isEmpty() ? Set.of()
                : new HashSet<>(loans.findBookIdsByUser(userId, LoanStatus.ACTIVE, ids));
        Set<Long> reserved = ids.isEmpty() ? Set.of()
                : new HashSet<>(reservations.findBookIdsByUser(userId, ReservationStatus.WAITING, ids));
        List<BookDto> content = p.getContent().stream()
                .map(b -> toDto(b, borrowed.contains(b.getId()), reserved.contains(b.getId()))).toList();
        return new PageResponse<>(content, p.getNumber(), p.getSize(), p.getTotalPages(), p.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return books.findCategories();
    }

    @Transactional
    public BookDto create(BookRequest r) {
        String isbn = r.isbn().trim();
        if (books.existsByIsbnAndDeletedFalse(isbn))
            throw new ApiException(HttpStatus.CONFLICT, "A book with this ISBN already exists");
        Book b = new Book();
        apply(b, r);
        b.setAvailableCopies(r.totalCopies());
        return toDto(books.save(b), false, false);
    }

    @Transactional
    public BookDto update(Long id, BookRequest r) {
        Book b = books.findLockedById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found"));
        if (books.existsByIsbnAndDeletedFalseAndIdNot(r.isbn().trim(), id))
            throw new ApiException(HttpStatus.CONFLICT, "A book with this ISBN already exists");
        int onLoan = b.getTotalCopies() - b.getAvailableCopies();
        if (r.totalCopies() < onLoan)
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Total copies cannot be less than the copies currently on loan (" + onLoan + ")");
        apply(b, r);
        b.setAvailableCopies(r.totalCopies() - onLoan);
        loanService.fulfillQueue(b); // new copies go to waiting users first
        return toDto(b, false, false);
    }

    @Transactional
    public void delete(Long id) {
        Book b = books.findLockedById(id).filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found"));
        if (loans.existsByBookAndStatus(b, LoanStatus.ACTIVE))
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete: some copies are currently on loan");
        if (reservations.existsByBookAndStatus(b, ReservationStatus.WAITING))
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete: there is a reservation queue for this book");
        b.setDeleted(true); // soft delete keeps loan history and fines intact
        b.setIsbn(b.getIsbn() + "#deleted-" + b.getId());
    }

    private void apply(Book b, BookRequest r) {
        b.setTitle(r.title().trim());
        b.setAuthor(r.author().trim());
        b.setIsbn(r.isbn().trim());
        b.setCategoryCode(r.categoryCode().trim());
        b.setTotalCopies(r.totalCopies());
    }

    private BookDto toDto(Book b, boolean borrowedByMe, boolean reservedByMe) {
        long waiting = reservations.countByBookAndStatus(b, ReservationStatus.WAITING);
        return new BookDto(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCategoryCode(),
                b.getTotalCopies(), b.getAvailableCopies(), waiting, borrowedByMe, reservedByMe);
    }
}
