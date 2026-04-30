package com.didgo.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StartFocusSessionResponse(
        @Schema(description = "?앹꽦??吏묒쨷???덈젴 ?몄뀡 ID?낅땲?? ?꾨즺 ?붿껌??path 媛믪쑝濡??ъ슜?⑸땲??", example = "40")
        long sessionId,
        @Schema(description = "?대쾲 ?몄뀡?먯꽌 ?뚮젅?댄븷 吏묒쨷???덈젴 ?④퀎?낅땲??", example = "1")
        int level,
        @Schema(description = "?몄뀡 ?쒗븳 ?쒓컙?낅땲?? ?⑥쐞??珥덉엯?덈떎.", example = "180")
        int durationSeconds,
        @Schema(description = "?대쾲 ?몄뀡?먯꽌 ?쒖꽌?濡??섑뻾??吏??紐⑸줉?낅땲??")
        List<FocusCommandResponse> commands
) {
}
