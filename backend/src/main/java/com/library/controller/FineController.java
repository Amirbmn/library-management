package com.library.controller;

import com.library.dto.Dtos.FineSummary;
import com.library.security.AppUserDetails;
import com.library.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;

    @GetMapping("/me")
    public FineSummary mine(@AuthenticationPrincipal AppUserDetails me) {
        return fineService.summary(me.getId());
    }

    @PostMapping("/pay")
    public FineSummary pay(@AuthenticationPrincipal AppUserDetails me) {
        return fineService.pay(me.getId());
    }
}
