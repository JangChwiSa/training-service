package com.jangchwisa.trainingservice.training.summary.dto;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrainingSessionListItemResponse(
        @Schema(description = "완료된 훈련 세션 ID입니다. 상세 조회 API의 path 값으로 사용할 수 있습니다.", example = "10")
        long sessionId,
        @Schema(description = "세션에 연결된 시나리오 ID입니다. 시나리오가 없는 훈련 유형이면 null일 수 있습니다.", example = "1")
        Long scenarioId,
        @Schema(description = "세션에 연결된 시나리오 제목입니다. 시나리오가 없는 훈련 유형이면 null일 수 있습니다.")
        String scenarioTitle,
        @Schema(description = "안전 훈련 카테고리입니다. 안전 훈련이 아니면 null입니다.", example = "COMMUTE_SAFETY")
        SafetyCategory category,
        @Schema(description = "훈련 점수입니다. 0부터 100 사이 값이며 아직 점수가 없으면 null입니다.", example = "85")
        Integer score,
        @Schema(description = "훈련 피드백 요약입니다. 피드백이 없는 훈련 유형이면 null일 수 있습니다.")
        String feedbackSummary,
        @Schema(description = "정답 수입니다. 안전/문서 훈련이 아니면 null일 수 있습니다.", example = "7")
        Integer correctCount,
        @Schema(description = "전체 문제 또는 선택 수입니다. 안전/문서 훈련이 아니면 null일 수 있습니다.", example = "10")
        Integer totalCount,
        @Schema(description = "플레이한 집중력 훈련 단계입니다. 집중력 훈련이 아니면 null입니다.", example = "2")
        Integer playedLevel,
        @Schema(description = "집중력 훈련 정확도입니다. 단위는 퍼센트이며 집중력 훈련이 아니면 null입니다.", example = "92.5")
        BigDecimal accuracyRate,
        @Schema(description = "집중력 훈련 오답 수입니다. 집중력 훈련이 아니면 null입니다.", example = "3")
        Integer wrongCount,
        @Schema(description = "집중력 훈련 평균 반응 시간입니다. 단위는 밀리초(ms)이며 집중력 훈련이 아니면 null입니다.", example = "820")
        Integer averageReactionMs,
        @Schema(description = "훈련 완료 시각입니다.", example = "2026-04-27T10:00:00")
        LocalDateTime completedAt
) {
}
