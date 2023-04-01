package com.chatsit.chat.model;

public class ModelPartyChat {
    String ChatId,userId,msg;

    public ModelPartyChat() {
    }

    public ModelPartyChat(String chatId, String userId, String msg) {
        ChatId = chatId;
        this.userId = userId;
        this.msg = msg;
    }

    public String getChatId() {
        return ChatId;
    }

    public void setChatId(String chatId) {
        ChatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
