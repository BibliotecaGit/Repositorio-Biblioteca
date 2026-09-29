package com.aurora.service;

import com.aurora.dto.book.BookRequest;
import com.aurora.dto.book.BookResponse;
import com.aurora.dto.rental.RentalResponse;
import com.aurora.entity.User;
import com.aurora.entity.enums.UserType;
import com.aurora.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RentalServiceTest {

    @Autowired
    private RentalService rentalService;

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
                .nome("User Rental 1")
                .email("user1." + System.currentTimeMillis() + "@test.com")
                .senhaHash("hash")
                .tipo(UserType.aluno)
                .build());

        user2 = userRepository.save(User.builder()
                .nome("User Rental 2")
                .email("user2." + System.currentTimeMillis() + "@test.com")
                .senhaHash("hash")
                .tipo(UserType.aluno)
                .build());

        BookRequest req = new BookRequest();
        req.setTitulo("Livro Aluguel Teste " + System.currentTimeMillis());
        req.setAutor("Autor Aluguel");
        req.setGenero("Drama");
        req.setNumPaginas(120);
        req.setValorLivro(BigDecimal.valueOf(40.00));
        req.setPrecoAluguel(BigDecimal.valueOf(8.00));

        book = bookService.createBook(req);
    }

    @Test
    public void testRentAvailableBook() {
        RentalResponse rental = rentalService.rentBook(user1.getId(), book.getId());

        assertNotNull(rental.getId());
        assertEquals(user1.getId(), rental.getUserId());
        assertEquals(book.getId(), rental.getBookId());
        assertEquals(0, BigDecimal.valueOf(8.00).compareTo(rental.getPrecoCobrado()));
    }

    @Test
    public void testMaxThreeCopiesRentable() {
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
            rentalService.rentBook(u.getId(), book.getId());
        }

        // 4th user attempting to rent should fail because no copies available
        User user4 = userRepository.save(User.builder()
                .nome("User 4")
                .email("user4." + System.currentTimeMillis() + "@test.com")
                .senhaHash("hash")
                .tipo(UserType.aluno)
                .build());

        assertThrows(IllegalStateException.class, () -> rentalService.rentBook(user4.getId(), book.getId()));
    }

    @Test
    public void testExtendRental() {
        RentalResponse rental = rentalService.rentBook(user1.getId(), book.getId());
        RentalResponse extended = rentalService.extendRental(rental.getId(), user1.getId());

        assertEquals(1, extended.getExtensaoContador());

        RentalResponse extendedSecond = rentalService.extendRental(rental.getId(), user1.getId());
        assertEquals(2, extendedSecond.getExtensaoContador());

        // 3rd extension attempt fails
        assertThrows(IllegalStateException.class, () -> rentalService.extendRental(rental.getId(), user1.getId()));
    }
}
