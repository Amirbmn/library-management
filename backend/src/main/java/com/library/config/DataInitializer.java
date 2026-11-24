package com.library.config;

import com.library.model.Book;
import com.library.model.Role;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final BookRepository books;
    private final PasswordEncoder encoder;

    @Value("${library.admin-username}") private String adminUsername;
    @Value("${library.admin-email}") private String adminEmail;
    @Value("${library.admin-password}") private String adminPassword;
    @Value("${library.seed-sample-books:false}") private boolean seed;

    @Override
    public void run(String... args) {
        if (!users.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(encoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
        if (seed && books.count() == 0) {
            add("Clean Code", "Robert C. Martin", "9780132350884", "CS", 3);
            add("Effective Java", "Joshua Bloch", "9780134685991", "CS", 2);
            add("Design Patterns", "Erich Gamma", "9780201633610", "CS", 1);
            add("The Pragmatic Programmer", "Andrew Hunt", "9780135957059", "CS", 2);
            add("1984", "George Orwell", "9780451524935", "FIC", 4);
            add("Brave New World", "Aldous Huxley", "9780060850524", "FIC", 1);
            add("A Brief History of Time", "Stephen Hawking", "9780553380163", "SCI", 2);
            add("Sapiens", "Yuval Noah Harari", "9780062316097", "HIS", 2);
        }
    }

    private void add(String title, String author, String isbn, String cat, int copies) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setCategoryCode(cat);
        b.setTotalCopies(copies);
        b.setAvailableCopies(copies);
        books.save(b);
    }
}
