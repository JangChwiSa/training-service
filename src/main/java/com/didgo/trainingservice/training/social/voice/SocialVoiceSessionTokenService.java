package com.didgo.trainingservice.training.social.voice;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SocialVoiceSessionTokenService {

    public static final int EXPIRES_IN_SECONDS = 300;

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, SocialVoiceSessionToken> tokens = new ConcurrentHashMap<>();
    private final Clock clock;

    public SocialVoiceSessionTokenService(Clock clock) {
        this.clock = clock;
    }

    public SocialVoiceSessionToken issue(
            long sessionId,
            long userId,
            long scenarioId,
            String scenarioContext,
            String openingScript,
            String openingAudioUrl
    ) {
        removeExpiredTokens();
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SocialVoiceSessionToken sessionToken = new SocialVoiceSessionToken(
                token,
                sessionId,
                userId,
                scenarioId,
                scenarioContext,
                openingScript,
                openingAudioUrl,
                Instant.now(clock).plus(Duration.ofSeconds(EXPIRES_IN_SECONDS))
        );
        tokens.put(token, sessionToken);
        return sessionToken;
    }

    public Optional<SocialVoiceSessionToken> consume(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SocialVoiceSessionToken sessionToken = tokens.remove(token);
        if (sessionToken == null || isExpired(sessionToken)) {
            return Optional.empty();
        }
        return Optional.of(sessionToken);
    }

    private void removeExpiredTokens() {
        Iterator<SocialVoiceSessionToken> iterator = tokens.values().iterator();
        while (iterator.hasNext()) {
            if (isExpired(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private boolean isExpired(SocialVoiceSessionToken token) {
        return !token.expiresAt().isAfter(Instant.now(clock));
    }
}
