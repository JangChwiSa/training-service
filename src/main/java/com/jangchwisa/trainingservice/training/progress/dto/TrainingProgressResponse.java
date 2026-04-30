package com.jangchwisa.trainingservice.training.progress.dto;

public sealed interface TrainingProgressResponse
        permits DocumentProgressResponse, FocusProgressResponse, SafetyProgressResponse, SocialProgressResponse,
        TrainingLevelResponse, TrainingProgressSummaryResponse {
}
