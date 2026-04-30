package com.didgo.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record CompleteFocusSessionResponse(
        @Schema(description = "?꾨즺 泥섎━??吏묒쨷???덈젴 ?몄뀡 ID?낅땲??", example = "40")
        long sessionId,
        @Schema(description = "吏묒쨷???덈젴 ?먯닔?낅땲?? 0遺??100 ?ъ씠 媛믪엯?덈떎.", example = "92")
        int score,
        @Schema(description = "?뺥솗?꾩엯?덈떎. ?⑥쐞???쇱꽱?몄엯?덈떎.", example = "92.5")
        BigDecimal accuracyRate,
        @Schema(description = "?ㅻ떟 諛섏쓳 ?섏엯?덈떎.", example = "3")
        int wrongCount,
        @Schema(description = "?됯퇏 諛섏쓳 ?쒓컙?낅땲?? ?⑥쐞??諛由ъ큹(ms)?낅땲??", example = "820")
        int averageReactionMs,
        @Schema(description = "?대쾲 寃곌낵濡??ㅼ쓬 ?④퀎媛 ?덈줈 ?닿툑?먮뒗吏 ?щ??낅땲??", example = "true")
        boolean unlockedNextLevel,
        @Schema(description = "?꾨즺 ???ъ슜???꾩옱 吏묒쨷???④퀎?낅땲??", example = "3")
        int currentLevel,
        @Schema(description = "?꾨즺 ???ъ슜?먭? ?뚮젅?댄븷 ???덈뒗 理쒓퀬 ?닿툑 ?④퀎?낅땲??", example = "3")
        int highestUnlockedLevel
) {
}
