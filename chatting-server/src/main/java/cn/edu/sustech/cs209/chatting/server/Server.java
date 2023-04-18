package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.Status;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static List<User> onlineUserList = new ArrayList<>();
    private static final int PORT = 1207;

    private static HashSet<ObjectOutputStream> writers = new HashSet<>();

    static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        logger.info("The SUSTech Chatting Server is running.");

        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Handler extends Thread {

        private final Socket socket;
        private ObjectInputStream input;
        private OutputStream os;
        private ObjectOutputStream output;
        private InputStream is;

        private User user;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                is = socket.getInputStream();
                input = new ObjectInputStream(is);
                os = socket.getOutputStream();
                output = new ObjectOutputStream(os);

                writers.add(output);

                User user = (User) input.readObject();
                this.user = user;
                onlineUserList.add(user);
                write(onlineUserList);

                logger.info(
                    "Now There Is A User That Has Connected To The Server: " + user.getName() + "\n"
                        + "The Whole Number Of Online Users: "
                        + onlineUserList.size());

                while (socket.isConnected()) {
                    Object o;
                    o = input.readObject();
                    if (o != null) {
                        if (o instanceof Message) {
                            Message message = (Message) o;
                            write(message);
                            logger.info(
                                "There Is A Message From " + message.getSentBy().getName() + " To "
                                    + message.getSendTo().getName() + ": " + message.getData());
                        }
                    }
                }
            } catch (SocketException e) {
                user.setStatus(Status.AWAY);
                writers.remove(output);
                onlineUserList.remove(user);
                try {
                    write(onlineUserList);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ClientDownPrompt();
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

        public void ClientDownPrompt() {
            logger.info(
                "Now There Is A User That Has Disconnected To The Server: " + user.getName() + "\n"
                    + "The Whole Number Of Online Users: "
                    + onlineUserList.size());
        }

        private static void write(Object o) throws IOException {
            for (ObjectOutputStream writer : writers) {
                writer.writeObject(o);
                writer.flush();
                writer.reset();
            }
        }
    }


}
