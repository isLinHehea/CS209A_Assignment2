package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static final int PORT = 1207;

    static Logger logger = LoggerFactory.getLogger(Server.class);


    public static void main(String[] args) throws IOException {
        logger.info("The Chatting Server Is Running.");
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
        private final Logger logger = LoggerFactory.getLogger(Handler.class);
        private ObjectInputStream input;
        private OutputStream os;
        private ObjectOutputStream output;
        private InputStream is;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            logger.info("Attempting To Connect A User...");
            try {
                is = socket.getInputStream();
                input = new ObjectInputStream(is);
                os = socket.getOutputStream();
                output = new ObjectOutputStream(os);


            } catch (SocketException socketException) {
            } catch (Exception e) {
            }
        }
    }
}
