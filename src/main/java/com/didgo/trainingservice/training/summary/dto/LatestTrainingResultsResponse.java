package com.didgo.trainingservice.training.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LatestTrainingResultsResponse(
        @Schema(description = "議고쉶 ????ъ슜??ID?낅땲??", example = "1")
        long userId,
        @Schema(description = "?ъ슜?먯쓽 理쒖떊 ?꾨즺 ?덈젴 寃곌낵 紐⑸줉?낅땲??")
        List<LatestTrainingResultResponse> results
) {
}
