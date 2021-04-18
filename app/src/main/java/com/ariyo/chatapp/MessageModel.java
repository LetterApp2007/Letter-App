package com.ariyo.chatapp;

public class MessageModel {

    String Uid, message;
    Long timestamp;

    public MessageModel(String uid, String message, Long timestamp) {
        Uid = uid;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessageModel(String uid, String message) {
        Uid = uid;
        this.message = message;
    }
    public MessageModel(){}

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
