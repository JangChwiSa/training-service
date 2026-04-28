package com.jangchwisa.trainingservice.config;

import com.jangchwisa.trainingservice.common.security.CurrentUserArgumentResolver;
import com.jangchwisa.trainingservice.common.security.TrustedUserHeaderProperties;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(TrustedUserHeaderProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final TrustedUserHeaderProperties trustedUserHeaderProperties;

    public WebMvcConfig(TrustedUserHeaderProperties trustedUserHeaderProperties) {
        this.trustedUserHeaderProperties = trustedUserHeaderProperties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver(trustedUserHeaderProperties));
    }
}
