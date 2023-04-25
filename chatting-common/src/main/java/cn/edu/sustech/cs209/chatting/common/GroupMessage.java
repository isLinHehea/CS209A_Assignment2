package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

/**
 * .
 */
public class GroupMessage implements Serializable {

    private static final long serialVersionUID = -3325557028698116649L;

    private final User sentBy;

    private final String data;

    public GroupMessage(User sentBy, String data) {
        this.sentBy = sentBy;
        this.data = data;
    }

    public User getSentBy() {
        return sentBy;
    }

    public String getData() {
        return data;
    }
}

