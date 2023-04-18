package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = -853434091929591296L;
    private String name;
    private String password;

    private Status status;

    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return name.equals(user.name);
    }


    public User(String name, String password, Status status) {
        this.name = name;
        this.password = password;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
