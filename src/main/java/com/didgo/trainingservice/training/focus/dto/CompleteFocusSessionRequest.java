package com.didgo.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CompleteFocusSessionRequest(
        @Schema(description = "吏묒쨷???덈젴 以?湲곕줉???꾩껜 諛섏쓳 濡쒓렇?낅땲?? ?몄뀡 ?쒖옉 ?묐떟??媛?command?????諛섏쓳???쒖텧?⑸땲??")
        @NotEmpty List<@Valid FocusReactionRequest> reactions
) {
}
