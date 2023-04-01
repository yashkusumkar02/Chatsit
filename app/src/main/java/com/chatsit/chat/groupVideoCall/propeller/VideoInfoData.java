package com.chatsit.chat.groupVideoCall.propeller;

import androidx.annotation.NonNull;

public class VideoInfoData {

    public final int mWidth;
    public final int mHeight;
    public final int mDelay;
    public final int mFrameRate;
    public final int mBitRate;
    public final int mCodec;

    public VideoInfoData(int width, int height, int delay, int frameRate, int bitRate, int codec) {
        this.mWidth = width;
        this.mHeight = height;
        this.mDelay = delay;
        this.mFrameRate = frameRate;
        this.mBitRate = bitRate;
        this.mCodec = codec;
    }

    public VideoInfoData(int width, int height, int delay, int frameRate, int bitRate) {
        this(width, height, delay, frameRate, bitRate, 0);
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoInfoData{" +
                "mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mDelay=" + mDelay +
                ", mFrameRate=" + mFrameRate +
                ", mBitRate=" + mBitRate +
                ", mCodec=" + mCodec +
                '}';
    }
}