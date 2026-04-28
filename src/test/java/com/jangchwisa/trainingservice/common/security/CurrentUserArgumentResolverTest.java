package com.jangchwisa.trainingservice.common.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jangchwisa.trainingservice.common.exception.GlobalExceptionHandler;
import com.jangchwisa.trainingservice.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class CurrentUserArgumentResolverTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resolvesCurrentUserFromTrustedHeader() throws Exception {
        mockMvc.perform(get("/test/current-user")
                        .header("X-User-Id", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(123))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void returnsUnauthorizedWhenTrustedHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/test/current-user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").value("Trusted user header is missing."));
    }

    @Test
    void returnsUnauthorizedWhenTrustedHeaderIsInvalid() throws Exception {
        mockMvc.perform(get("/test/current-user")
                        .header("X-User-Id", "not-a-number"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").value("Trusted user header is invalid."));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/current-user")
        ApiResponse<CurrentUser> currentUser(@AuthenticatedUser CurrentUser currentUser) {
            return ApiResponse.success(currentUser);
        }
    }
}
