package com.aurora.repository;

import com.aurora.entity.Book;
import com.aurora.entity.enums.BookSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    List<Book> findByDeletedAtIsNull();

    List<Book> findByDataLancamentoAfterAndDeletedAtIsNull(LocalDate date);

    List<Book> findByEhInfantilTrueAndDeletedAtIsNull();

    List<Book> findByFaixaTamanhoAndDeletedAtIsNull(BookSize size);

    List<Book> findByGeneroAndDeletedAtIsNull(String genero);

    List<Book> findByAutorAndDeletedAtIsNull(String autor);

    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND b.avaliacaoMedia >= 4.5 ORDER BY b.alugueisUltimoAno DESC")
    List<Book> findDestaques();

    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL AND (LOWER(b.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.autor) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.genero) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Book> searchBooks(String query);
}
