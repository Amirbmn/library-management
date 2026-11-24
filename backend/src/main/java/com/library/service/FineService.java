package com.library.service;

import com.library.dto.Dtos.FineSummary;
import com.library.exception.ApiException;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.User;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class FineService {
    private final LoanRepository loans;
    private final UserRepository users;

    @Value("${library.fine-per-day:1.00}") private BigDecimal finePerDay;

    @Transactional(readOnly = true)
    public FineSummary summary(Long userId) {
        User user = users.findById(userId).orElseThrow();
        return compute(user);
    }

    /** Simulated payment: settles all fines of already returned books. */
    @Transactional
    public FineSummary pay(Long userId) {
        User user = users.findLockedById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        var unpaid = loans.findByUserAndStatusAndFinePaidFalse(user, LoanStatus.RETURNED);
        if (unpaid.isEmpty())
            throw new ApiException(HttpStatus.CONFLICT,
                    "You have no fines to pay. Overdue books must be returned first.");
        unpaid.forEach(l -> l.setFinePaid(true));
        return compute(user);
    }

    private FineSummary compute(User user) {
        BigDecimal unpaid = loans.findByUserAndStatusAndFinePaidFalse(user, LoanStatus.RETURNED).stream()
                .map(Loan::getFineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDate today = LocalDate.now();
        BigDecimal accrued = loans.findByUserAndStatus(user, LoanStatus.ACTIVE).stream()
                .filter(l -> today.isAfter(l.getDueDate()))
                .map(l -> finePerDay.multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(l.getDueDate(), today))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FineSummary(unpaid, accrued, unpaid.add(accrued));
    }
}
