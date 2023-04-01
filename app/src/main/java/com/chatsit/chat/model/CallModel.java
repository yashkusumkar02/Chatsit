package com.chatsit.chat.model;

public class CallModel {

    String from,to,room, call,type;

    public CallModel() {
    }

    public CallModel(String from, String to, String room, String call, String type) {
        this.from = from;
        this.to = to;
        this.room = room;
        this.call = call;
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
