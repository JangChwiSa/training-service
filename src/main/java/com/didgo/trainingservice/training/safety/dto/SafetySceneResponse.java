package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SafetySceneResponse(
        @Schema(description = "?덉쟾 ?덈젴 ?λ㈃ ID?낅땲?? ?ㅼ쓬 ?λ㈃ 吏꾪뻾 ?붿껌??sceneId濡??ъ슜?⑸땲??", example = "1")
        long sceneId,
        @Schema(description = "?꾨줎?멸? ?λ㈃???쒖떆????李멸퀬???붾㈃ ?뺣낫?낅땲??")
        String screenInfo,
        @Schema(description = "?꾩옱 ?덉쟾 ?곹솴 ?ㅻ챸?낅땲??")
        String situationText,
        @Schema(description = "?ъ슜?먯뿉寃??쒖떆??吏덈Ц?낅땲??")
        String questionText,
        @Schema(description = "?꾩옱 ?λ㈃?먯꽌 ?좏깮 媛?ν븳 ?좏깮吏 紐⑸줉?낅땲??")
        List<SafetyChoiceResponse> choices,
        @Schema(description = "?꾩옱 ?λ㈃??留덉?留??λ㈃?몄? ?щ??낅땲??", example = "true")
        boolean endScene
) {
}
