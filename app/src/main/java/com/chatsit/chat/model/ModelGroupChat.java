package com.chatsit.chat.model;

public class ModelGroupChat {

    String sender,msg,type,timestamp;

    public ModelGroupChat() {
    }

    public ModelGroupChat(String sender, String msg, String type, String timestamp) {
        this.sender = sender;
        this.msg = msg;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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
}
