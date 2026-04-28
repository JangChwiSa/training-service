package com.jangchwisa.trainingservice.training.social.dto;

import jakarta.validation.constraints.NotBlank;

public record SelectSocialJobTypeRequest(
        @NotBlank String jobType
) {
}
