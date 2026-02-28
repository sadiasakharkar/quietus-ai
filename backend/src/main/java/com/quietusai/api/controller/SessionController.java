package com.quietusai.api.controller;

import com.quietusai.api.dto.SessionResponse;
import com.quietusai.api.dto.StartSessionResponse;
import com.quietusai.application.service.SessionService;
import com.quietusai.security.service.AppUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public StartSessionResponse start(@AuthenticationPrincipal AppUserPrincipal principal) {
        return sessionService.startSession(principal.getUserId());
    }

    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SessionResponse end(@AuthenticationPrincipal AppUserPrincipal principal,
                               @PathVariable UUID sessionId) {
        return sessionService.endSession(principal.getUserId(), sessionId);
    }

    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SessionResponse get(@AuthenticationPrincipal AppUserPrincipal principal,
                               @PathVariable UUID sessionId) {
        return sessionService.getSession(principal.getUserId(), sessionId);
    }
}
