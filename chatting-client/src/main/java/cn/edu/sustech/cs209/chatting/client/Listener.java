package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.GroupMessage;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MyFile;
import cn.edu.sustech.cs209.chatting.common.Prompt;
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
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 */
public class Listener implements Runnable {

    private Socket socket;
    public String hostname;
    public int port;
    public static User user;
    public ChatController chatController;
    public GroupChatController groupChatController;
    private InputStream is;
    private static OutputStream os;
    private ObjectInputStream input;
    private static ObjectOutputStream output;
    private boolean ifFile = false;
    private boolean ifStop = true;

    private static FileOutputStream fos;
    private static DataInputStream dis;

    private MyFile sentFile;

    static Logger logger = LoggerFactory.getLogger(Listener.class);

    /**
     * .
     */
    public Listener(String hostname, int port, User user, ChatController chatController) {
        this.hostname = hostname;
        this.port = port;
        Listener.user = user;
        this.chatController = chatController;
        chatController.user = user;
    }

    /**
     * .
     */
    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            os = socket.getOutputStream();
            output = new ObjectOutputStream(os);
            is = socket.getInputStream();
            input = new ObjectInputStream(is);

            connect();

            while (socket.isConnected()) {
                if (ifFile && !ifStop) {
                    Platform.runLater(() -> filePrompt(sentFile));
                    ifStop = true;
                }
                if (!ifFile && ifStop) {
                    Object o;
                    o = input.readObject();
                    if (o != null) {
                        if (o instanceof List) {
                            Platform.runLater(() -> chatController.onlineUserList.setItems(
                                FXCollections.observableArrayList((List<User>) o)));
                        } else if (o instanceof Message message) {
                            if (message.getSentBy().equals(user)) {
                                chatController.addToChatContentListForBy(message);
                            } else if (message.getSendTo().equals(user)) {
                                chatController.addToChatContentListForTo(message);
                                Platform.runLater(() -> chatController.messagePrompt(message));
                            }
                        } else if (o instanceof Prompt prompt) {
                            if (prompt.getGroupMaster().equals(user)) {
                                ObservableList<User> groupChatUserList = FXCollections.observableArrayList(
                                    prompt.getGroupMembers());
                                FXMLLoader fxmlLoader = new FXMLLoader(
                                    getClass().getResource("views/GroupChatView.fxml"));
                                Scene scene = new Scene(fxmlLoader.load());
                                groupChatController = fxmlLoader.getController();
                                groupChatController.user = user;
                                groupChatController.showGroupChatView(groupChatUserList, scene);
                            } else if (prompt.getGroupMembers().contains(user)) {
                                ObservableList<User> groupChatUserList = FXCollections.observableArrayList(
                                    prompt.getGroupMembers());
                                groupChatUserList.remove(user);
                                groupChatUserList.add(prompt.getGroupMaster());
                                FXMLLoader fxmlLoader = new FXMLLoader(
                                    getClass().getResource("views/GroupChatView.fxml"));
                                Scene scene = new Scene(fxmlLoader.load());
                                groupChatController = fxmlLoader.getController();
                                groupChatController.user = user;
                                groupChatController.showGroupChatView(groupChatUserList, scene);
                            }
                        } else if (o instanceof GroupMessage message) {
                            groupChatController.addToChatContentList(message);
                        } else if (o instanceof User leaveUser) {
                            if (!leaveUser.equals(user)) {
                                groupChatController.removeLeavedUser(leaveUser);
                            }
                        } else if (o instanceof MyFile sentFile) {
                            if (sentFile.getReceiver().equals(user)) {
                                this.sentFile = sentFile;
                                ifFile = true;
                                ifStop = false;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Platform.runLater(() -> chatController.serverDownPrompt());
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

    /**
     * .
     */
    public static void sendFile(MyFile sentFile) throws IOException {
        output.writeObject(sentFile);
        output.flush();
        File file = sentFile.getFile();
        DataOutputStream dos = new DataOutputStream(os);
        logger.info("Start transferring file-----");

        dos.writeLong(file.length());
        dos.flush();

        byte[] bytes = new byte[1024];
        int length;
        long progress = 0;
        FileInputStream fis = new FileInputStream(file);
        while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
            dos.write(bytes, 0, length);
            dos.flush();
            progress += length;
            logger.info("| " + (100 * progress / file.length()) + "% |");
        }
        fis.close();
        logger.info("File transfer successful-----");
    }

    public static void sendForGroup(GroupMessage message) throws IOException {
        output.writeObject(message);
        output.flush();
    }

    public static void leaveGroup(User user) throws IOException {
        output.writeObject(user);
        output.flush();
    }

    /**
     * .
     */
    public static void createGroupChat(User groupMaster, List<User> groupMembers)
        throws IOException {
        Prompt prompt = new Prompt(groupMaster, groupMembers);
        output.writeObject(prompt);
        output.flush();
    }

    /**
     * .
     */
    public void filePrompt(MyFile sentFile) {
        Stage promptStage = new Stage();
        promptStage.initModality(Modality.APPLICATION_MODAL);
        promptStage.setTitle("FilePrompt");

        Label label = new Label(
            sentFile.getSender().getName() + " has sent you a file.");
        label.getStyleClass().add("label");
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #333;");

        Label fileLoc = new Label("Please select the folder you downloaded");
        fileLoc.getStyleClass().add("fileLoc");
        fileLoc.setStyle("-fx-font-size: 16px; -fx-text-fill: #333;");

        TextArea fileName = new TextArea();
        fileName.setPromptText("File Name");
        fileName.setPrefColumnCount(100);
        fileName.setPrefRowCount(30);

        Button selectFolderButton = new Button("Select Folder");
        selectFolderButton.setStyle(
            "-fx-background-color: #fee1b8; -fx-text-fill: #fff; -fx-font-weight: bold;");

        File[] file = new File[1];
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                // 线程执行内容
                try {
                    fos = new FileOutputStream(file[0]);
                    dis = new DataInputStream(is);
                    byte[] bytes = new byte[1024];
                    int length;
                    long fileLength = dis.readLong();
                    while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                        fos.write(bytes, 0, length);
                        fos.flush();
                        if (fileLength == file[0].length()) {
                            break;
                        }
                    }
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ifFile = false;
                Platform.runLater(promptStage::close);
                return null;
            }
        };
        selectFolderButton.setOnAction(event -> Platform.runLater(() -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File folder = directoryChooser.showDialog(
                chatController.chatContentList.getScene().getWindow());
            String folderPath = folder.getPath();
            fileLoc.setText(folderPath);
            String filePath = folderPath + "\\" + fileName.getText() + ".docx";
            file[0] = new File(filePath);
            if (!file[0].exists()) {
                try {
                    file[0].createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            new Thread(task).start();
            promptStage.close();
        }));
        selectFolderButton.setPrefWidth(120);
        selectFolderButton.setPrefHeight(35);

        VBox vbox = new VBox(10, label, fileName, selectFolderButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setStyle(
            "-fx-background-color: #fff; -fx-border-color: #fee1b8; -fx-border-width: 3px;");

        Scene promptScene = new Scene(vbox, 400, 200);
        promptStage.setScene(promptScene);

        promptStage.initStyle(StageStyle.TRANSPARENT);
        promptStage.setResizable(false);

        promptStage.show();
    }
}
