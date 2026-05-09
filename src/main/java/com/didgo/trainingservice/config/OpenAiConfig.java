package com.didgo.trainingservice.config;

import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.training.social.voice.realtime.OpenAiRealtimeProperties;
import com.didgo.trainingservice.training.social.voice.tts.OpenAiSpeechProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OpenAiProperties.class, OpenAiRealtimeProperties.class, OpenAiSpeechProperties.class})
public class OpenAiConfig {
}
