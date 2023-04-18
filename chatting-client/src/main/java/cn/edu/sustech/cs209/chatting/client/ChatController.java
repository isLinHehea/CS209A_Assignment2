package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatController implements Initializable {

    User user;
    @FXML
    TextArea messageArea;
    @FXML
    ListView<User> chatUserList;
    @FXML
    ListView<Message> chatContentList;

    List<User> onlineUserList = new ArrayList<>();

    User currentChatUser;

    HashMap<User, ArrayList<Message>> chatContent = new HashMap<>();

    Logger logger = LoggerFactory.getLogger(ChatController.class);


    @Override

    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatUserList.setCellFactory(new UserCellFactory());
        chatContentList.setCellFactory(new MessageCellFactory());
    }

    @FXML
    public void createPrivateChat() {
        AtomicReference<User> userSelected = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<User> userToBeSelected = new ComboBox<>();

        userToBeSelected.getItems().addAll(onlineUserList);
        userToBeSelected.getItems().remove(user);

        Button createBtn = new Button("Create");
        createBtn.setOnAction(e -> {
            User selected = userToBeSelected.getSelectionModel().getSelectedItem();
            if (selected != null) {
                userSelected.set(selected);
            }
            stage.close();
        });
        Label select = new Label("Select");
        select.setFont(new Font("Arial", 17));

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(select, userToBeSelected, createBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        currentChatUser = userSelected.get();
        chatUserList.getItems().add(currentChatUser);

        ArrayList<Message> chattingList = new ArrayList<>();
        chatContent.put(currentChatUser, chattingList);

        ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
            chatContent.get(currentChatUser));
        chatContentList.setItems(chattingRecords);

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name. You can select
     * several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat: If there are > 3 users: display the
     * first three usernames, sorted in lexicographic order, then use ellipsis with the number of
     * users, for example: UserA, UserB, UserC... (10) If there are <= 3 users: do not display the
     * ellipsis, for example: UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    @FXML
    public void sendMessage() throws IOException {
        String data = messageArea.getText();
        Message msg = new Message(user, currentChatUser, data);
        if (!messageArea.getText().isEmpty()) {
            Listener.send(msg);
            messageArea.clear();
            logger.info(
                "You have successfully sent a message to " + msg.getSendTo().getName() + ": "
                    + msg.getData());
        }
    }

    public void handleUserListClick(MouseEvent event) {
        currentChatUser = chatUserList.getSelectionModel().getSelectedItem();
        ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
            chatContent.get(currentChatUser));
        chatContentList.setItems(chattingRecords);
    }

    public class UserCellFactory implements Callback<ListView<User>, ListCell<User>> {

        @Override
        public ListCell<User> call(ListView<User> param) {
            return new ListCell<User>() {
                private HBox hbox = new HBox();
                private Text nameText = new Text();
                private Text statusText = new Text();
                private Circle statusCircle;


                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);

                    if (empty || user == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    Platform.runLater(() -> {
                        nameText.setFont(Font.font("Arial", 16));

                        statusText.setFont(Font.font("Arial", 12));
                        statusText.setFill(Color.GRAY);

                        statusCircle = new Circle(8);

                        if (!hbox.getChildren().contains(nameText) && !hbox.getChildren()
                            .contains(statusText)) {
                            hbox.getChildren().clear();

                            hbox = new HBox(10, statusCircle, nameText, statusText);
                            hbox.setAlignment(Pos.CENTER_LEFT);
                            hbox.setMinHeight(Region.USE_PREF_SIZE);
                            hbox.setMaxHeight(Region.USE_PREF_SIZE);
                            nameText.setText(user.getName());
                            statusText.setText(user.getStatus().toString());
                            statusCircle.setFill(Paint.valueOf(user.getStatus().getColor()));

                            Tooltip tooltip = new Tooltip(user.getStatus().getDescription());
                            Tooltip.install(hbox, tooltip);

                            setGraphic(hbox);
                        }
                    });
                }
            };
        }
    }

    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                private final HBox wrapper = new HBox();
                private final Label nameLabel = new Label();
                private final Label msgLabel = new Label();

                @Override
                protected void updateItem(Message msg, boolean empty) {
                    if (empty || msg == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    Platform.runLater(() -> {
                        nameLabel.setText(msg.getSentBy().getName());
                        nameLabel.setMaxWidth(Double.MAX_VALUE);
                        nameLabel.setWrapText(true);
                        nameLabel.setStyle("-fx-font-weight: bold;");

                        msgLabel.setText(msg.getData());
                        msgLabel.setWrapText(true);
                        msgLabel.setStyle(
                            "-fx-background-color: #f4f4f4; -fx-background-radius: 10px; -fx-padding: 10px;");

                        if (!wrapper.getChildren().contains(nameLabel) && !wrapper.getChildren()
                            .contains(msgLabel)) {
                            wrapper.getChildren().clear();

                            if (user.equals(msg.getSentBy())) {
                                wrapper.setAlignment(Pos.TOP_RIGHT);
                                wrapper.getChildren().addAll(msgLabel, nameLabel);
                            } else {
                                wrapper.setAlignment(Pos.TOP_LEFT);
                                wrapper.getChildren().addAll(nameLabel, msgLabel);
                            }

                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setGraphic(wrapper);
                        }
                    });
                }
            };
        }
    }

    public void addToChatContentListForBy(Message msg) {
        chatContent.get(msg.getSendTo()).add(msg);
        ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
            chatContent.get(msg.getSendTo()));
        chatContentList.setItems(chattingRecords);
    }

    public void addToChatContentListForTo(Message msg) {
        logger.info(
            "You have successfully received a message from " + msg.getSendTo().getName() + ": "
                + msg.getData());
        currentChatUser = msg.getSentBy();
        if (!chatUserList.getItems().contains(currentChatUser)) {
            chatUserList.getItems().add(currentChatUser);
            ArrayList<Message> chattingList = new ArrayList<>();
            chatContent.put(currentChatUser, chattingList);
        }
        chatContent.get(msg.getSentBy()).add(msg);
        ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
            chatContent.get(msg.getSentBy()));
        chatContentList.setItems(chattingRecords);
    }

    public void ServerDownPrompt() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Server Down");
        alert.setHeaderText("The server has been shut down.");
        alert.setContentText("Please try again later.");
        alert.showAndWait();
    }
}
