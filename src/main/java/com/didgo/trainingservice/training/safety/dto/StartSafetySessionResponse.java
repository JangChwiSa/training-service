package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StartSafetySessionResponse(
        @Schema(description = "?앹꽦???덉쟾 ?덈젴 ?몄뀡 ID?낅땲?? ?댄썑 ?ㅼ쓬 ?λ㈃, ?곸꽭 議고쉶, ?꾨즺 ?붿껌??path 媛믪쑝濡??ъ슜?⑸땲??", example = "20")
        long sessionId,
        @Schema(description = "?몄뀡 ?쒖옉 ??泥섏쓬 吏꾪뻾???덉쟾 ?덈젴 ?λ㈃?낅땲??")
        SafetySceneResponse scene
) {
}
