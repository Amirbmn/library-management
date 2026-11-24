package com.library.controller;

import com.library.dto.Dtos.LoanDto;
import com.library.security.AppUserDetails;
import com.library.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;

    @PostMapping("/borrow/{bookId}")
    public LoanDto borrow(@PathVariable Long bookId, @AuthenticationPrincipal AppUserDetails me) {
        return loanService.borrow(me.getId(), bookId);
    }

    @PostMapping("/{loanId}/return")
    public LoanDto giveBack(@PathVariable Long loanId, @AuthenticationPrincipal AppUserDetails me) {
        return loanService.giveBack(me.getId(), loanId);
    }

    @GetMapping("/me")
    public List<LoanDto> mine(@AuthenticationPrincipal AppUserDetails me) {
        return loanService.myLoans(me.getId());
    }
}
