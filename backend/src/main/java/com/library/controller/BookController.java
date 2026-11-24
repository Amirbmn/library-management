package com.library.controller;

import com.library.dto.Dtos.*;
import com.library.security.AppUserDetails;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping
    public PageResponse<BookDto> list(@RequestParam(defaultValue = "") String search,
                                      @RequestParam(defaultValue = "") String category,
                                      @RequestParam(defaultValue = "false") boolean availableOnly,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @AuthenticationPrincipal AppUserDetails me) {
        return bookService.search(search, category, availableOnly, page, size, me.getId());
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return bookService.categories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto create(@Valid @RequestBody BookRequest r) {
        return bookService.create(r);
    }

    @PutMapping("/{id}")
    public BookDto update(@PathVariable Long id, @Valid @RequestBody BookRequest r) {
        return bookService.update(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
