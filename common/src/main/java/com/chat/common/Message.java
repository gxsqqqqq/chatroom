package com.chat.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        BROADCAST,
        PRIVATE,
        USER_LIST,
        LOGIN,
        LOGOUT
    }

    private String sender;
    private String recipient;
    private String content;
    private Type type;
    private List<String> userList;
    private long timestamp;

    public Message(String sender, String recipient, String content, Type type) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(List<String> userList) {
        this.type = Type.USER_LIST;
        this.userList = userList;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}