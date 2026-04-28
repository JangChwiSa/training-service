package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;

public record SelectSocialJobTypeResponse(
        SocialJobType jobType,
        String nextPage
) {
}
