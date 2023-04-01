package com.chatsit.chat.faceFilters;

@SuppressWarnings("ALL")
public interface CameraGrabberListener {
    void onCameraInitialized();
    void onCameraError(String errorMsg);
}
