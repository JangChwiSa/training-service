package com.didgo.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FocusCommandResponse(
        @Schema(description = "吏묒쨷???덈젴 吏??ID?낅땲?? ?꾨즺 ?붿껌??commandId濡??ъ슜?⑸땲??", example = "1001")
        long commandId,
        @Schema(description = "?몄뀡 ?덉뿉??吏?쒓? ?섑??섎뒗 ?쒖꽌?낅땲??", example = "1")
        int order,
        @Schema(description = "?ъ슜?먯뿉寃?蹂댁뿬以?吏??臾멸뎄?낅땲??", example = "泥?린 ?ㅼ뼱")
        String commandText,
        @Schema(description = "?뺣떟?쇰줈 ?몄젙?섎뒗 湲곕? ?됰룞 媛믪엯?덈떎.", example = "BLUE_UP")
        String expectedAction,
        @Schema(description = "?몄뀡 ?쒖옉 ??吏?쒓? ?쒖떆???쒖젏?낅땲?? ?⑥쐞??諛由ъ큹(ms)?낅땲??", example = "0")
        int displayAtMs
) {
}
