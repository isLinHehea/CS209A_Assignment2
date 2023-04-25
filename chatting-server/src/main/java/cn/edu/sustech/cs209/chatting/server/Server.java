package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.GroupMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MyFile;
import cn.edu.sustech.cs209.chatting.common.Prompt;
import cn.edu.sustech.cs209.chatting.common.Status;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * .
 */
public class Server {

    private static final List<User> onlineUserList = new ArrayList<>();
    private static final int PORT = 1207;

    private static final HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static final HashMap<User, FileInputStream> fises = new HashMap<>();
    private static final HashMap<User, DataOutputStream> doses = new HashMap<>();

    static Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * .
     */
    public static void main(String[] args) {
        logger.info("The SUSTech Chatting Server is running.");

//        onlineUserList.add(new User("TESTA", "1234567", Status.ONLINE));
//        onlineUserList.add(new User("TESTB", "1234567", Status.ONLINE));

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
        private final File file;
        private User user;

        public Handler(Socket socket) {
            this.socket = socket;
            file = new File("D:\\serverFile.docx");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

                FileInputStream fis = new FileInputStream(file);
                DataOutputStream dos = new DataOutputStream(os);

                fises.put(user, fis);
                doses.put(user, dos);

                logger.info(
                    "There Is A User That Has Connected To The Server: " + user.getName() + "\n"
                        + "--The Whole Number Of Online Users: "
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
                        } else if (o instanceof Prompt) {
                            Prompt prompt = (Prompt) o;
                            write(prompt);
                            logger.info(
                                "There is a new group chat, the group master is: "
                                    + prompt.getGroupMaster().getName());
                        } else if (o instanceof GroupMessage) {
                            GroupMessage message = (GroupMessage) o;
                            write(message);
                            logger.info(
                                "There Is A Message From The Group from" + message.getSentBy()
                                    .getName() + ": " + message.getData());
                        } else if (o instanceof User) {
                            User leaveUser = (User) o;
                            write(leaveUser);
                            logger.info(
                                "There Is A User Leave The Group: " + leaveUser.getName());
                        } else if (o instanceof MyFile) {
                            MyFile sentFile = (MyFile) o;
                            saveFile(file);
                            writeFile(sentFile, file);
                        }
                    }
                }
            } catch (SocketException e) {
                user.setStatus(Status.AWAY);
                writers.remove(output);
                fises.remove(user);
                doses.remove(user);
                onlineUserList.remove(user);
                try {
                    write(onlineUserList);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                clientDownPrompt();
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

        private void clientDownPrompt() {
            logger.info(
                "There Is A User That Has Disconnected To The Server: " + user.getName() + "\n"
                    + "--The Whole Number Of Online Users: "
                    + onlineUserList.size());
        }

        private void saveFile(File file) throws IOException {
            FileOutputStream fos = new FileOutputStream(file);
            DataInputStream dis = new DataInputStream(is);
            byte[] bytes = new byte[1024];
            int length;
            long fileLength = dis.readLong();
            while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                fos.write(bytes, 0, length);
                fos.flush();
                if (fileLength == file.length()) {
                    break;
                }
            }

            fos.close();
        }

        private static void write(Object o) throws IOException {
            for (ObjectOutputStream writer : writers) {
                writer.writeObject(o);
                writer.flush();
                writer.reset();
            }
        }

        private static void writeFile(MyFile sentFile, File file) throws IOException {
            write(sentFile);
            DataOutputStream dos = doses.get(sentFile.getReceiver());
            dos.writeLong(file.length());
            dos.flush();
            logger.info("Start transferring file-----");
            byte[] bytes = new byte[1024];
            int length;
            long progress = 0;
            FileInputStream fis = fises.get(sentFile.getReceiver());
            while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                dos.write(bytes, 0, length);
                dos.flush();
                progress += length;
                logger.info("| " + (100 * progress / file.length()) + "% |");
            }
            logger.info("File transfer successful-----");
            fis.close();
        }
    }
}