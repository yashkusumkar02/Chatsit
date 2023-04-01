package com.chatsit.chat.model;

public class ChatModel {

    private String sender;
    private String receiver;
    private String msg;
    private String type;
    private String timestamp;
    private boolean isSeen;

    public ChatModel(String sender, String receiver, String msg, String type, boolean isSeen, String timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.msg = msg;
        this.type = type;
        this.isSeen = isSeen;
        this.timestamp = timestamp;
    }

    public ChatModel() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
