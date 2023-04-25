package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;

/**
 * .
 */
public class Prompt implements Serializable {

    private final User groupMaster;

    private final List<User> groupMembers;

    private static final long serialVersionUID = -6607193428990648462L;

    public Prompt(User groupMaster, List<User> groupMembers) {
        this.groupMaster = groupMaster;
        this.groupMembers = groupMembers;
    }

    public User getGroupMaster() {
        return groupMaster;
    }

    public List<User> getGroupMembers() {
        return groupMembers;
    }
}
