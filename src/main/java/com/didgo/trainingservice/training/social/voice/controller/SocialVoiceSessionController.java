package com.didgo.trainingservice.training.social.voice.controller;

import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionService;
import com.didgo.trainingservice.training.social.voice.dto.SocialVoiceSessionPrepareResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SocialVoiceSessionController {

    private final SocialVoiceSessionService socialVoiceSessionService;

    public SocialVoiceSessionController(SocialVoiceSessionService socialVoiceSessionService) {
        this.socialVoiceSessionService = socialVoiceSessionService;
    }

    @Operation(
            summary = "Prepare social voice session",
            description = "Validates a social training session and returns opening voice metadata plus a short-lived WebSocket token."
    )
    @PostMapping("/api/trainings/social/sessions/{sessionId}/voice/prepare")
    public ApiResponse<SocialVoiceSessionPrepareResponse> prepare(
            @AuthenticatedUser CurrentUser currentUser,
            @Parameter(description = "Social training session ID.", example = "10")
            @PathVariable long sessionId
    ) {
        return ApiResponse.success(socialVoiceSessionService.prepare(currentUser, sessionId));
    }
}
