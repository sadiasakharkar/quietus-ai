package com.quietusai.api.controller;

import com.quietusai.api.dto.*;
import com.quietusai.application.service.FusionService;
import com.quietusai.security.service.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/chunks/{chunkId}")
public class PredictionController {

    private final FusionService fusionService;

    public PredictionController(FusionService fusionService) {
        this.fusionService = fusionService;
    }

    @PostMapping("/vision")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PredictionAckResponse upsertVision(@AuthenticationPrincipal AppUserPrincipal principal,
                                              @PathVariable UUID sessionId,
                                              @PathVariable UUID chunkId,
                                              @Valid @RequestBody UpsertVisionPredictionRequest request) {
        return fusionService.upsertVision(principal.getUserId(), sessionId, chunkId, request);
    }

    @PostMapping("/audio")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PredictionAckResponse upsertAudio(@AuthenticationPrincipal AppUserPrincipal principal,
                                             @PathVariable UUID sessionId,
                                             @PathVariable UUID chunkId,
                                             @Valid @RequestBody UpsertAudioPredictionRequest request) {
        return fusionService.upsertAudio(principal.getUserId(), sessionId, chunkId, request);
    }

    @PostMapping("/text")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PredictionAckResponse upsertText(@AuthenticationPrincipal AppUserPrincipal principal,
                                            @PathVariable UUID sessionId,
                                            @PathVariable UUID chunkId,
                                            @Valid @RequestBody UpsertTextPredictionRequest request) {
        return fusionService.upsertText(principal.getUserId(), sessionId, chunkId, request);
    }

    @PostMapping("/fuse")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public FusionResultResponse fuse(@AuthenticationPrincipal AppUserPrincipal principal,
                                     @PathVariable UUID sessionId,
                                     @PathVariable UUID chunkId) {
        return fusionService.fuse(principal.getUserId(), sessionId, chunkId);
    }

    @GetMapping("/fusion")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public FusionResultResponse getFusion(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @PathVariable UUID sessionId,
                                          @PathVariable UUID chunkId) {
        return fusionService.getFusion(principal.getUserId(), sessionId, chunkId);
    }
}
