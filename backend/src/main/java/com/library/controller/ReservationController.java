package com.library.controller;

import com.library.dto.Dtos.ReservationDto;
import com.library.security.AppUserDetails;
import com.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService service;

    @PostMapping("/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDto reserve(@PathVariable Long bookId, @AuthenticationPrincipal AppUserDetails me) {
        return service.reserve(me.getId(), bookId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails me) {
        service.cancel(me.getId(), id);
    }

    @GetMapping("/me")
    public List<ReservationDto> mine(@AuthenticationPrincipal AppUserDetails me) {
        return service.mine(me.getId());
    }
}
