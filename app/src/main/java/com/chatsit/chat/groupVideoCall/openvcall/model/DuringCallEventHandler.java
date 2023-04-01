package com.chatsit.chat.groupVideoCall.openvcall.model;

@SuppressWarnings("ALL")
public interface DuringCallEventHandler extends AGEventHandler {

    void onUserJoined(int uid);

    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onExtraCallback(int type, Object... data);
}
