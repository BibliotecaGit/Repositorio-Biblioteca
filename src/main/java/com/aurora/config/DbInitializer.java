package com.aurora.config;

import com.aurora.dto.book.BookRequest;
import com.aurora.entity.User;
import com.aurora.entity.enums.UserType;
import com.aurora.repository.UserRepository;
import com.aurora.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DbInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookService bookService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            // Seed Users
            userRepository.save(User.builder()
                    .nome("Admin Aurora")
                    .email("admin@aurora.local")
                    .senhaHash(passwordEncoder.encode("admin123"))
                    .tipo(UserType.administrador)
                    .build());

            userRepository.save(User.builder()
                    .nome("Bibliotecario Aurora")
                    .email("biblio@aurora.local")
                    .senhaHash(passwordEncoder.encode("biblio123"))
                    .tipo(UserType.bibliotecario)
                    .build());

            userRepository.save(User.builder()
                    .nome("Aluno Teste")
                    .email("aluno@aurora.local")
                    .senhaHash(passwordEncoder.encode("aluno123"))
                    .tipo(UserType.aluno)
                    .build());

            // Seed Books
            BookRequest b1 = new BookRequest();
            b1.setTitulo("Dom Casmurro");
            b1.setAutor("Machado de Assis");
            b1.setGenero("Romance");
            b1.setNumPaginas(208);
            b1.setValorLivro(BigDecimal.valueOf(45.00));
            b1.setPrecoAluguel(BigDecimal.valueOf(7.50));
            b1.setCapaUrl("https://example.com/domcasmurro.jpg");
            b1.setDataLancamento(LocalDate.now().minusMonths(2));
            b1.setEhInfantil(false);
            bookService.createBook(b1);

            BookRequest b2 = new BookRequest();
            b2.setTitulo("O Pequeno Príncipe");
            b2.setAutor("Antoine de Saint-Exupéry");
            b2.setGenero("Infantil");
            b2.setNumPaginas(96);
            b2.setValorLivro(BigDecimal.valueOf(30.00));
            b2.setPrecoAluguel(BigDecimal.valueOf(5.00));
            b2.setCapaUrl("https://example.com/pequenoprincipe.jpg");
            b2.setDataLancamento(LocalDate.now().minusMonths(1));
            b2.setEhInfantil(true);
            bookService.createBook(b2);

            BookRequest b3 = new BookRequest();
            b3.setTitulo("1984");
            b3.setAutor("George Orwell");
            b3.setGenero("Ficção Científica");
            b3.setNumPaginas(328);
            b3.setValorLivro(BigDecimal.valueOf(55.00));
            b3.setPrecoAluguel(BigDecimal.valueOf(9.00));
            b3.setCapaUrl("https://example.com/1984.jpg");
            b3.setDataLancamento(LocalDate.now().minusYears(1));
            b3.setEhInfantil(false);
            bookService.createBook(b3);
        }
    }
}
