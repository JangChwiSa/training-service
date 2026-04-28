package com.jangchwisa.trainingservice.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "training.security")
public class TrustedUserHeaderProperties {

    private String trustedUserIdHeader = "X-User-Id";

    public String trustedUserIdHeader() {
        return trustedUserIdHeader;
    }

    public void setTrustedUserIdHeader(String trustedUserIdHeader) {
        this.trustedUserIdHeader = trustedUserIdHeader;
    }
}
