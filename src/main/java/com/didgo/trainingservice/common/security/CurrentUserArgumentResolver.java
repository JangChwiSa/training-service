package com.didgo.trainingservice.common.security;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final TrustedUserHeaderProperties properties;

    public CurrentUserArgumentResolver(TrustedUserHeaderProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUser.class)
                && CurrentUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        String headerName = properties.trustedUserIdHeader();
        String rawUserId = webRequest.getHeader(headerName);

        if (!StringUtils.hasText(rawUserId)) {
            throw new TrainingServiceException(ErrorCode.UNAUTHORIZED, "Trusted user header is missing.");
        }

        try {
            long userId = Long.parseLong(rawUserId);
            if (userId <= 0) {
                throw new NumberFormatException("userId must be positive");
            }
            return new CurrentUser(userId);
        } catch (NumberFormatException exception) {
            throw new TrainingServiceException(ErrorCode.UNAUTHORIZED, "Trusted user header is invalid.");
        }
    }
}
