package com.aurora.controller;

import com.aurora.dto.book.BookRequest;
import com.aurora.dto.book.BookResponse;
import com.aurora.dto.common.MessageResponse;
import com.aurora.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(bookService.searchBooks(search));
        }
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/novidades")
    public ResponseEntity<List<BookResponse>> getNovidades() {
        return ResponseEntity.ok(bookService.getNovidades());
    }

    @GetMapping("/destaques")
    public ResponseEntity<List<BookResponse>> getDestaques() {
        return ResponseEntity.ok(bookService.getDestaques());
    }

    @GetMapping("/infantis")
    public ResponseEntity<List<BookResponse>> getInfantis() {
        return ResponseEntity.ok(bookService.getInfantis());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMINISTRADOR')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMINISTRADOR')")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MessageResponse> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(new MessageResponse("Livro excluído com sucesso", true));
    }
}
