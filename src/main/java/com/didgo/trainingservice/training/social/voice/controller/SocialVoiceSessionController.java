package com.didgo.trainingservice.training.social.voice.controller;

import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.security.AuthenticatedUser;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionService;
import com.didgo.trainingservice.training.social.voice.dto.OpeningVoiceResponse;
import com.didgo.trainingservice.training.social.voice.dto.SocialVoiceSessionPrepareResponse;
import com.didgo.trainingservice.training.social.voice.tts.SocialOpeningAudioAsset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;

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

    @Operation(
            summary = "Prepare social scenario opening audio",
            description = "Returns the counterpart opening request and a cached OpenAI TTS audio URL."
    )
    @PostMapping("/api/trainings/social/scenarios/{scenarioId}/opening-audio")
    public ApiResponse<OpeningVoiceResponse> openingAudio(
            @Parameter(description = "Social scenario ID.", example = "1")
            @PathVariable long scenarioId
    ) {
        return ApiResponse.success(socialVoiceSessionService.openingVoice(scenarioId));
    }

    @Operation(summary = "Download cached social opening audio")
    @GetMapping("/api/trainings/social/opening-audio/{cacheKey}.{extension}")
    public ResponseEntity<byte[]> openingAudioAsset(
            @PathVariable String cacheKey,
            @PathVariable String extension
    ) {
        SocialOpeningAudioAsset asset = socialVoiceSessionService.openingAudioAsset(cacheKey);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .body(asset.audioData());
    }
}
