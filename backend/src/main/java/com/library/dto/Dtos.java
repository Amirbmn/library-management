package com.library.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 6, max = 100) String password) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record AuthResponse(String token, Long id, String username, String role) {}

    public record BookRequest(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 255) String author,
            @NotBlank @Size(max = 40) String isbn,
            @NotBlank @Size(max = 50) String categoryCode,
            @NotNull @Min(0) Integer totalCopies) {}

    public record BookDto(Long id, String title, String author, String isbn, String categoryCode,
                          int totalCopies, int availableCopies, long waitingCount,
                          boolean borrowedByMe, boolean reservedByMe) {}

    public record LoanDto(Long id, Long bookId, String bookTitle, String bookAuthor,
                          LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate,
                          String status, boolean overdue, long daysLate,
                          BigDecimal fine, boolean finePaid) {}

    public record ReservationDto(Long id, Long bookId, String bookTitle, String bookAuthor,
                                 long position, LocalDateTime createdAt) {}

    public record FineSummary(BigDecimal unpaidReturned, BigDecimal accruedOverdue, BigDecimal total) {}

    public record PageResponse<T>(List<T> content, int page, int size, int totalPages, long totalElements) {}

    public record MessageResponse(String message) {}
}
