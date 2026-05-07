package com.didgo.trainingservice.config;

import com.didgo.trainingservice.training.social.voice.websocket.SocialVoiceSessionHandshakeInterceptor;
import com.didgo.trainingservice.training.social.voice.websocket.SocialVoiceSessionWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SocialVoiceWebSocketConfig implements WebSocketConfigurer {

    private final SocialVoiceSessionWebSocketHandler handler;
    private final SocialVoiceSessionHandshakeInterceptor handshakeInterceptor;

    public SocialVoiceWebSocketConfig(
            SocialVoiceSessionWebSocketHandler handler,
            SocialVoiceSessionHandshakeInterceptor handshakeInterceptor
    ) {
        this.handler = handler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/trainings/social/voice")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
