package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

public record SocialDialogLogRequest(
        @Schema(description = "?????踰덊샇?낅땲?? 媛숈? ?댁쓽 ?ъ슜??諛쒗솕? AI ?묐떟? 媛숈? 踰덊샇瑜??ъ슜?????덉뒿?덈떎.", example = "1")
        @Positive int turnNo,
        @Schema(description = "諛쒗솕?먯엯?덈떎. USER???ъ슜?? AI??AI ?묐떟?낅땲??", example = "USER")
        @NotNull SocialDialogSpeaker speaker,
        @Schema(description = "?대떦 ?댁쓽 ?ㅼ젣 ????댁슜?낅땲?? 鍮?臾몄옄?댁? ?ъ슜?????놁뒿?덈떎.", example = "Can you help me?")
        @NotBlank String content
) {
}
