package com.didgo.trainingservice.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.GlobalExceptionHandler;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class CommonApiResponseTest {

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsCommonSuccessResponse() throws Exception {
        mockMvc.perform(get("/test/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("ok"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void returnsCommonErrorResponseForServiceException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Scenario was not found."));
    }

    @Test
    void returnsValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Request validation failed."));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/success")
        ApiResponse<TestResponse> success() {
            return ApiResponse.success(new TestResponse("ok"));
        }

        @GetMapping("/test/not-found")
        ApiResponse<Void> notFound() {
            throw new TrainingServiceException(ErrorCode.NOT_FOUND, "Scenario was not found.");
        }

        @PostMapping("/test/validation")
        ApiResponse<TestResponse> validation(@Valid @RequestBody TestRequest request) {
            return ApiResponse.success(new TestResponse(request.name()));
        }
    }

    record TestRequest(@NotBlank String name) {
    }

    record TestResponse(String message) {
    }
}
