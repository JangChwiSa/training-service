package com.didgo.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FocusReactionRequest(
        @Schema(description = "諛섏쓳??吏??ID?낅땲?? 吏묒쨷???몄뀡 ?쒖옉 ?묐떟??commands 諛곗뿴?먯꽌 諛쏆? commandId瑜??ｌ뒿?덈떎.", example = "1001")
        @Positive long commandId,
        @Schema(description = "?ъ슜?먭? ?ㅼ젣濡??섑뻾???낅젰?낅땲?? expectedAction怨?鍮꾧탳???뺣떟 ?щ?瑜?怨꾩궛?⑸땲??", example = "BLUE_UP")
        @NotBlank String userInput,
        @Schema(description = "吏?쒓? ?쒖떆?????ъ슜?먭? 諛섏쓳?섍린源뚯? 嫄몃┛ ?쒓컙?낅땲?? ?⑥쐞??諛由ъ큹(ms)?낅땲??", example = "720")
        @PositiveOrZero int reactionMs
) {
}
