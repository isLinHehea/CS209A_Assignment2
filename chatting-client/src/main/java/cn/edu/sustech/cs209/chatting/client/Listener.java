package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;


public class Listener implements Runnable {

    private Socket socket;
    public String hostname;
    public int port;
    public static User user;
    public ChatController chatController;
    private InputStream is;
    private OutputStream os;
    private ObjectInputStream input;
    private static ObjectOutputStream output;

    public Listener(String hostname, int port, User user, ChatController chatController) {
        this.hostname = hostname;
        this.port = port;
        Listener.user = user;
        this.chatController = chatController;
        chatController.user = user;
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

            chatController.onlineUserList = (List<User>) input.readObject();

            while (socket.isConnected()) {
                Object o;
                o = input.readObject();
                if (o != null) {
                    if (o instanceof List) {
                        chatController.onlineUserList = (List<User>) o;
                    } else if (o instanceof Message) {
                        Message message = (Message) o;
                        if (message.getSentBy().equals(user)) {
                            chatController.addToChatContentListForBy(message);
                        } else if (message.getSendTo().equals(user)) {
                            chatController.addToChatContentListForTo(message);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Platform.runLater(() -> {
                chatController.ServerDownPrompt();
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect() throws IOException {
        output.writeObject(user);
    }

    public static void send(Message message) throws IOException {
        output.writeObject(message);
        output.flush();
    }
}
