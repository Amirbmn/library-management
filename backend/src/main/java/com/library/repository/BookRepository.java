package com.library.repository;

import com.library.model.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findLockedById(@Param("id") Long id);

    @Query("select b from Book b where b.deleted = false " +
           "and (lower(b.title) like :q or lower(b.author) like :q) " +
           "and (:cat = '' or b.categoryCode = :cat) " +
           "and (:avail = false or b.availableCopies > 0)")
    Page<Book> search(@Param("q") String q, @Param("cat") String cat,
                      @Param("avail") boolean avail, Pageable pageable);

    boolean existsByIsbnAndDeletedFalse(String isbn);
    boolean existsByIsbnAndDeletedFalseAndIdNot(String isbn, Long id);

    @Query("select distinct b.categoryCode from Book b where b.deleted = false order by b.categoryCode")
    List<String> findCategories();
}
