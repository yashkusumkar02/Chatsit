package com.chatsit.chat.groupVoiceCall.propeller;

import io.agora.rtc.RtcEngine;

@SuppressWarnings("ALL")
public class Constant {


    public static final String MEDIA_SDK_VERSION;

    static {
        String sdk = "undefined";
        try {
            sdk = RtcEngine.getSdkVersion();
        } catch (Throwable e) {
        }
        MEDIA_SDK_VERSION = sdk;
    }
}

