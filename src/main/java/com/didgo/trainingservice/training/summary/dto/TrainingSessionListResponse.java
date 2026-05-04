package com.didgo.trainingservice.training.summary.dto;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record TrainingSessionListResponse(
        @Schema(description = "議고쉶???덈젴 ?좏삎?낅땲??", example = "SOCIAL")
        TrainingType trainingType,
        @Schema(description = "?꾩옱 ?섏씠吏 踰덊샇?낅땲?? 0遺???쒖옉?⑸땲??", example = "0")
        int page,
        @Schema(description = "?섏씠吏????ぉ ?섏엯?덈떎.", example = "10")
        int size,
        @Schema(description = "議곌굔??留욌뒗 ?꾩껜 ?꾨즺 ?몄뀡 ?섏엯?덈떎.", example = "3")
        long totalElements,
        @Schema(description = "?꾩옱 ?섏씠吏???덈젴 湲곕줉 紐⑸줉?낅땲??")
        List<TrainingSessionListItemResponse> sessions
) {
}
