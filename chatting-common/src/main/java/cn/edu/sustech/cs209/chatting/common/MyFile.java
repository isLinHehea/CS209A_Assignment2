package cn.edu.sustech.cs209.chatting.common;

import java.io.File;
import java.io.Serializable;

/**
 * .
 */
public class MyFile implements Serializable {

    private static final long serialVersionUID = -5935126601730370325L;

    private final File file;

    private final User receiver;

    private final User sender;

    /**
     * .
     */
    public MyFile(File file, User sender, User receiver) {
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
    }

    public File getFile() {
        return file;
    }

    public User getReceiver() {
        return receiver;
    }

    public User getSender() {
        return sender;
    }
}
