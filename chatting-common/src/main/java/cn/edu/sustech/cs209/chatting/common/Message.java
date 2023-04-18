package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = -7755162995496909499L;

    private final User sentBy;

    private final User sendTo;

    private final String data;

    public Message(User sentBy, User sendTo, String data) {
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public User getSentBy() {
        return sentBy;
    }

    public User getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
}
