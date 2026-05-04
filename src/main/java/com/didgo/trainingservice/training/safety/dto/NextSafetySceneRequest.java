package com.didgo.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record NextSafetySceneRequest(
        @Schema(description = "?꾩옱 ?ъ슜?먭? ?좏깮吏瑜?怨좊Ⅸ ?λ㈃ ID?낅땲?? ?몄뀡 ?쒖옉 ?먮뒗 ?댁쟾 next-scene ?묐떟??sceneId瑜??ｌ뒿?덈떎.", example = "1")
        @Positive long sceneId,
        @Schema(description = "?ъ슜?먭? ?좏깮???좏깮吏 ID?낅땲?? ?꾩옱 ?λ㈃??choices 諛곗뿴?먯꽌 諛쏆? choiceId瑜??ｌ뒿?덈떎.", example = "1")
        @Positive long choiceId
) {
}
