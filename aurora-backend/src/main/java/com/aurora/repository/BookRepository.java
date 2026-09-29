package com.aurora.repository;

import com.aurora.entity.Book;
import com.aurora.entity.enums.BookSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIdAndDeletedAtIsNull(Long id);

    List<Book> findAllByDeletedAtIsNull();

    Page<Book> findAllByDeletedAtIsNull(Pageable pageable);

    List<Book> findByDataLancamentoAfterAndDeletedAtIsNull(LocalDate date);

    List<Book> findByEhInfantilTrueAndDeletedAtIsNull();

    List<Book> findByFaixaTamanhoAndDeletedAtIsNull(BookSize size);

    List<Book> findByGeneroIgnoreCaseAndDeletedAtIsNull(String genero);

    List<Book> findByAutorIgnoreCaseAndDeletedAtIsNull(String autor);

    List<Book> findTop10ByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND b.avaliacaoMedia >= 4.5 ORDER BY b.alugueisUltimoAno DESC")
    List<Book> findDestaques();

    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND (LOWER(b.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.autor) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.genero) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Book> searchActiveBooks(@Param("query") String query);
}
