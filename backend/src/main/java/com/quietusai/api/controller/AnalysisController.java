package com.quietusai.api.controller;

import com.quietusai.api.dto.SubmitAnalysisRequest;
import com.quietusai.api.dto.SubmitAnalysisResponse;
import com.quietusai.application.service.AnalysisService;
import com.quietusai.security.service.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SubmitAnalysisResponse submit(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @Valid @RequestBody SubmitAnalysisRequest request) {
        return analysisService.analyze(principal.getUserId(), request);
    }
}
