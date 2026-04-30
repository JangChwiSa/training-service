package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NextSafetySceneResponse(
        @Schema(description = "諛⑷툑 ?쒖텧???좏깮???뺣떟 ?щ??낅땲??")
        SafetySelectedResultResponse selectedResult,
        @Schema(description = "?ㅼ쓬??吏꾪뻾???λ㈃?낅땲?? 留덉?留??λ㈃?대㈃ null?????덉뒿?덈떎.")
        SafetySceneResponse nextScene
) {
}
