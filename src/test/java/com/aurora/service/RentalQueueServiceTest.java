package com.aurora.service;

import com.aurora.dto.book.BookRequest;
import com.aurora.dto.book.BookResponse;
import com.aurora.dto.queue.PendingResponse;
import com.aurora.dto.queue.WaitlistResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.User;
import com.aurora.entity.enums.UserType;
import com.aurora.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RentalQueueServiceTest {

    @Autowired
    private RentalQueueService rentalQueueService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private BookResponse book;

    @BeforeEach
    public void setup() {
        user1 = userRepository.save(User.builder()
                .nome("User Queue 1")
                .email("user1." + System.currentTimeMillis() + "@test.com")
                .senhaHash("hash")
                .tipo(UserType.aluno)
                .build());

        user2 = userRepository.save(User.builder()
                .nome("User Queue 2")
                .email("user2." + System.currentTimeMillis() + "@test.com")
                .senhaHash("hash")
                .tipo(UserType.aluno)
                .build());

        BookRequest req = new BookRequest();
        req.setTitulo("Livro Fila Teste " + System.currentTimeMillis());
        req.setAutor("Autor Fila");
        req.setGenero("Drama");
        req.setNumPaginas(120);
        req.setValorLivro(BigDecimal.valueOf(40.00));
        req.setPrecoAluguel(BigDecimal.valueOf(8.00));

        book = bookService.createBook(req);
    }

    @Test
    public void testRentAvailableBook() {
        RentalResponse rental = rentalQueueService.rentBook(user1, book.getId());

        assertNotNull(rental.getId());
        assertEquals(user1.getId(), rental.getUserId());
        assertEquals(book.getId(), rental.getBookId());
        assertEquals(0, BigDecimal.valueOf(8.00).compareTo(rental.getPrecoCobrado()));
    }

    @Test
    public void testQueueLimitAndPendingPromotion() {
        // Exhaust 3 copies by renting with 3 distinct users
        List<User> renters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User u = userRepository.save(User.builder()
                    .nome("Renter " + i)
                    .email("renter." + i + "." + System.currentTimeMillis() + "@test.com")
                    .senhaHash("hash")
                    .tipo(UserType.aluno)
                    .build());
            renters.add(u);
            rentalQueueService.rentBook(u, book.getId());
        }

        // Fill queue with 10 users
        List<User> waitlisters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User u = userRepository.save(User.builder()
                    .nome("Waitlister " + i)
                    .email("waitlister." + i + "." + System.currentTimeMillis() + "@test.com")
                    .senhaHash("hash")
                    .tipo(UserType.aluno)
                    .build());
            waitlisters.add(u);
            WaitlistResponse w = rentalQueueService.enterQueue(u, book.getId());
            assertEquals(i + 1, w.getPosicao());
        }

        // 11th user cannot enter queue (limit 10)
        User user11 = userRepository.save(User.builder()
                .nome("User 11")
                .email("user11." + System.currentTimeMillis() + "@test.com")
                .senhaHash("hash")
                .tipo(UserType.aluno)
                .build());

        assertThrows(ResponseStatusException.class, () -> rentalQueueService.enterQueue(user11, book.getId()));

        // 11th user enters Pending List
        PendingResponse p = rentalQueueService.enterPending(user11, book.getId());
        assertNotNull(p.getId());
        assertEquals(user11.getId(), p.getUserId());
    }
}
