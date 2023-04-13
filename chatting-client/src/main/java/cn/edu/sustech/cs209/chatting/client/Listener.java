package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.User;

import java.io.*;
import java.net.Socket;


public class Listener implements Runnable {

    private Socket socket;
    public String hostname;
    public int port;
    public User user;
    public ChatController controller;
    private InputStream is;
    private OutputStream os;
    private ObjectInputStream input;
    private static ObjectOutputStream output;

    public Listener(String hostname, int port, User user, ChatController controller) {
        this.hostname = hostname;
        this.port = port;
        this.user = user;
        this.controller = controller;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            os = socket.getOutputStream();
            output = new ObjectOutputStream(os);
            is = socket.getInputStream();
            input = new ObjectInputStream(is);

            connect();
        } catch (IOException e) {
        }
    }

    public void connect() throws IOException {
        output.writeObject(user);
    }
}
