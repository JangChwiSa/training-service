package com.didgo.trainingservice.training.social.voice.realtime;

public interface RealtimeUpstreamEventHandler {

    void onEvent(String eventJson);

    void onError(Throwable throwable);

    void onClose();
}
