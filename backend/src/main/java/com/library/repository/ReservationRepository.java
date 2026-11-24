package com.library.repository;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findFirstByBookAndStatusOrderByIdAsc(Book book, ReservationStatus status);
    boolean existsByUserAndBookAndStatus(User user, Book book, ReservationStatus status);
    boolean existsByBookAndStatus(Book book, ReservationStatus status);
    long countByBookAndStatus(Book book, ReservationStatus status);
    long countByBookAndStatusAndIdLessThan(Book book, ReservationStatus status, Long id);
    List<Reservation> findByUserAndStatusOrderByIdAsc(User user, ReservationStatus status);

    @Query("select r.book.id from Reservation r where r.user.id = :uid and r.status = :status and r.book.id in :ids")
    List<Long> findBookIdsByUser(@Param("uid") Long uid, @Param("status") ReservationStatus status,
                                 @Param("ids") Collection<Long> ids);
}
