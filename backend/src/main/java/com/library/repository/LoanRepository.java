package com.library.repository;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findLockedById(@Param("id") Long id);

    long countByUserAndStatus(User user, LoanStatus status);
    boolean existsByUserAndBookAndStatus(User user, Book book, LoanStatus status);
    boolean existsByBookAndStatus(Book book, LoanStatus status);
    boolean existsByUserAndStatusAndDueDateBefore(User user, LoanStatus status, LocalDate date);
    List<Loan> findByUserOrderByIdDesc(User user);
    List<Loan> findByUserAndStatus(User user, LoanStatus status);
    List<Loan> findByUserAndStatusAndFinePaidFalse(User user, LoanStatus status);

    @Query("select l.book.id from Loan l where l.user.id = :uid and l.status = :status and l.book.id in :ids")
    List<Long> findBookIdsByUser(@Param("uid") Long uid, @Param("status") LoanStatus status,
                                 @Param("ids") Collection<Long> ids);
}
