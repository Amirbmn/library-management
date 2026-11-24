package com.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter @Setter @NoArgsConstructor
public class Loan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate borrowDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    /** false only when a fine was charged and is not yet settled */
    @Column(nullable = false)
    private boolean finePaid = true;
}
