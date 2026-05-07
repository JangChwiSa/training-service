package com.didgo.trainingservice.training.social.voice.websocket;

import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionToken;
import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionTokenService;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SocialVoiceSessionHandshakeInterceptor implements HandshakeInterceptor {

    public static final String TOKEN_ATTRIBUTE = "socialVoiceSessionToken";

    private final SocialVoiceSessionTokenService tokenService;

    public SocialVoiceSessionHandshakeInterceptor(SocialVoiceSessionTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");
        return tokenService.consume(token)
                .map(sessionToken -> putToken(attributes, sessionToken))
                .orElse(false);
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }

    private boolean putToken(Map<String, Object> attributes, SocialVoiceSessionToken token) {
        attributes.put(TOKEN_ATTRIBUTE, token);
        return true;
    }
}
