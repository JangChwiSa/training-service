package com.didgo.trainingservice.training.progress.dto;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FocusProgressResponse(
        @Schema(description = "?덈젴 ?좏삎?낅땲??", example = "FOCUS")
        TrainingType trainingType,
        @Schema(description = "?ъ슜?먯쓽 ?꾩옱 吏묒쨷???덈젴 ?④퀎?낅땲??", example = "3")
        int currentLevel,
        @Schema(description = "?ъ슜?먭? ?뚮젅?댄븷 ???덈뒗 理쒓퀬 ?닿툑 ?④퀎?낅땲??", example = "3")
        int highestUnlockedLevel,
        @Schema(description = "媛??理쒓렐???뚮젅?댄븳 ?④퀎?낅땲?? 湲곕줉???놁쑝硫?null?낅땲??", example = "2")
        Integer lastPlayedLevel,
        @Schema(description = "理쒓렐 ?뚮젅???뺥솗?꾩엯?덈떎. ?⑥쐞???쇱꽱?몄씠硫?湲곕줉???놁쑝硫?null?낅땲??", example = "92.5")
        BigDecimal lastAccuracyRate,
        @Schema(description = "理쒓렐 ?뚮젅???됯퇏 諛섏쓳 ?쒓컙?낅땲?? ?⑥쐞??諛由ъ큹(ms)?대ŉ 湲곕줉???놁쑝硫?null?낅땲??", example = "820")
        Integer lastAverageReactionMs,
        @Schema(description = "吏묒쨷??吏꾪뻾 ?곹깭媛 留덉?留됱쑝濡?媛깆떊???쒓컖?낅땲??", example = "2026-04-27T11:00:00")
        LocalDateTime updatedAt
) implements TrainingProgressResponse {
}
