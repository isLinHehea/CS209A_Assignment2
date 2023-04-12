package cn.edu.sustech.cs209.chatting.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;


public class Listener implements Runnable {

    private Socket socket;
    public String hostname;
    public int port;
    public static String username;
    public ChatController controller;
    private InputStream is;
    private OutputStream os;
    private ObjectInputStream input;
    private static ObjectOutputStream output;

    Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, String username, ChatController controller) {
        this.hostname = hostname;
        this.port = port;
        Listener.username = username;
        this.controller = controller;
    }

    public void run() {
        try {
//            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
//            os = socket.getOutputStream();
//            output = new ObjectOutputStream(os);
//            is = socket.getInputStream();
//            input = new ObjectInputStream(is);
        } catch (IOException e) {

        }
    }
}
