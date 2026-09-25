package com.aurora.service;

import com.aurora.dto.book.BookRequest;
import com.aurora.dto.book.BookResponse;
import com.aurora.entity.enums.BookSize;
import com.aurora.repository.BookCopyRepository;
import com.aurora.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Test
    public void testPageSizeTiers() {
        // Pequeno <= 100
        BookRequest req1 = createBookReq("Pequeno", 80);
        BookResponse resp1 = bookService.createBook(req1);
        assertEquals(BookSize.pequeno, resp1.getFaixaTamanho());

        // Médio-pequeno 101..150
        BookRequest req2 = createBookReq("Médio Pequeno", 120);
        BookResponse resp2 = bookService.createBook(req2);
        assertEquals(BookSize.medio_pequeno, resp2.getFaixaTamanho());

        // Médio-padrão 151..250
        BookRequest req3 = createBookReq("Médio Padrão", 200);
        BookResponse resp3 = bookService.createBook(req3);
        assertEquals(BookSize.medio_padrao, resp3.getFaixaTamanho());

        // Grande > 250
        BookRequest req4 = createBookReq("Grande", 350);
        BookResponse resp4 = bookService.createBook(req4);
        assertEquals(BookSize.grande, resp4.getFaixaTamanho());
    }

    @Test
    public void testThreeCopiesCreatedByDefault() {
        BookRequest req = createBookReq("Copias Test", 100);
        BookResponse resp = bookService.createBook(req);

        assertEquals(3, resp.getTotalCopias());
        assertEquals(3, resp.getCopiasDisponiveis());
        assertTrue(resp.getCanAlugar());
        assertFalse(resp.getCanFila());
        assertFalse(resp.getCanPendente());
    }

    private BookRequest createBookReq(String title, int pages) {
        BookRequest req = new BookRequest();
        req.setTitulo(title);
        req.setAutor("Autor Teste");
        req.setGenero("Gênero Teste");
        req.setNumPaginas(pages);
        req.setValorLivro(BigDecimal.valueOf(50.00));
        req.setPrecoAluguel(BigDecimal.valueOf(10.00));
        return req;
    }
}
