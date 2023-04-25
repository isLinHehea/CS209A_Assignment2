package cn.edu.sustech.cs209.chatting.common;

/**
 * .
 */
public enum Status {
    ONLINE("Green", "Online"),
    AWAY("Orange", "Away");

    private final String color;
    private final String description;

    Status(String color, String description) {
        this.color = color;
        this.description = description;
    }

    public String getColor() {
        return color;
    }
}
