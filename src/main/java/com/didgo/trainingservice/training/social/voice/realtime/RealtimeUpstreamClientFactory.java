package com.didgo.trainingservice.training.social.voice.realtime;

import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionToken;

public interface RealtimeUpstreamClientFactory {

    RealtimeUpstreamClient connect(SocialVoiceSessionToken token, RealtimeUpstreamEventHandler eventHandler);
}
