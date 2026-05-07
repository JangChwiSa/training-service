package com.didgo.trainingservice.training.social.voice.realtime;

public interface RealtimeUpstreamClient {

    void send(String eventJson);

    void close();
}
