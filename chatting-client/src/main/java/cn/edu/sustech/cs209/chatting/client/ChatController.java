package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatController implements Initializable {

    User user;
    @FXML
    TextArea messageArea;
    @FXML
    ListView<User> chatUserList;
    @FXML
    ListView<User> onlineUserList;
    @FXML
    ListView<Message> chatContentList;

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

        userToBeSelected.getItems().addAll(onlineUserList.getItems());
        userToBeSelected.getItems().remove(user);

        Button createBtn = new Button("Create");
        createBtn.setOnAction(e -> {
            User selected = userToBeSelected.getSelectionModel().getSelectedItem();
            if (selected != null) {
                userSelected.set(selected);
                currentChatUser = userSelected.get();
                if (!chatUserList.getItems().contains(currentChatUser)) {
                    chatUserList.getItems().add(currentChatUser);
                    ArrayList<Message> chattingList = new ArrayList<>();
                    chatContent.put(currentChatUser, chattingList);
                }
                ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
                    chatContent.get(currentChatUser));
                chatContentList.setItems(chattingRecords);
                logger.info("The current chat user is: " + currentChatUser.getName());
            } else {
                return;
            }
            stage.close();
        });

        HBox box = new HBox(10, userToBeSelected, createBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        stage.setScene(new Scene(box));
        stage.showAndWait();
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
        Stage stage = new Stage();

        ListView<User> listView = new ListView<>();
        listView.setItems(onlineUserList.getItems());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button createBtn = new Button("Create");
        createBtn.setOnAction(e -> {
            ObservableList<User> selectedItems = listView.getSelectionModel().getSelectedItems();
            if (!selectedItems.isEmpty() && selectedItems.size() != 1) {
                try {
                    Stage GroupChatStage = new Stage();
                    FXMLLoader fxmlLoader = new FXMLLoader(
                        getClass().getResource("views/GroupChatView.fxml"));
                    GroupChatStage.setScene(new Scene(fxmlLoader.load()));
                    GroupChatStage.setResizable(false);
                    GroupChatStage.show();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                return;
            }
            stage.close();
        });

        VBox box = new VBox(10, listView, createBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 10, 20));
        stage.setScene(new Scene(box));
        stage.showAndWait();
    }

    @FXML
    public void sendMessage() throws IOException {
        if (currentChatUser != null && messageArea.getText() != null) {
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
    }

    public void handleUserListClick() {
        if (chatUserList.getSelectionModel().getSelectedItem() != null) {
            currentChatUser = chatUserList.getSelectionModel().getSelectedItem();
            ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
                chatContent.get(currentChatUser));
            logger.info(Integer.toString(chatContent.get(currentChatUser).size()));
            chatContentList.setItems(chattingRecords);
            logger.info("The current chat user is: " + currentChatUser.getName());
        }
    }

    public static class UserCellFactory implements Callback<ListView<User>, ListCell<User>> {

        @Override
        public ListCell<User> call(ListView<User> param) {
            return new ListCell<User>() {

                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);

                    if (empty || Objects.isNull(user)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    HBox wrapper = new HBox();
                    Text nameText = new Text();
                    Text statusText = new Text();
                    Circle statusCircle = new Circle(7);

                    {
                        nameText.setFont(Font.font("Arial", 16));
                        statusText.setFont(Font.font("Arial", 12));
                        statusText.setFill(Color.GRAY);
                    }

                    nameText.setText(user.getName());
                    statusText.setText(user.getStatus().toString());
                    statusCircle.setFill(Paint.valueOf(user.getStatus().getColor()));
                    wrapper.setAlignment(Pos.CENTER_LEFT);
                    wrapper.setMinHeight(Region.USE_PREF_SIZE);
                    wrapper.setMaxHeight(Region.USE_PREF_SIZE);
                    wrapper.getChildren().addAll(statusCircle, nameText, statusText);
                    wrapper.setSpacing(12);

                    Platform.runLater(() -> {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        setGraphic(wrapper);
                    });
                }
            };
        }
    }

    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {


                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label();
                    Label msgLabel = new Label();

                    {
                        nameLabel.setMaxWidth(Double.MAX_VALUE);
                        nameLabel.setWrapText(true);
                        nameLabel.setStyle("-fx-font-weight: bold;");

                        msgLabel.setWrapText(true);
                        msgLabel.setStyle(
                            "-fx-background-color: #f4f4f4; -fx-background-radius: 10px; -fx-padding: 10px;");
                    }

                    nameLabel.setText(msg.getSentBy().getName());
                    msgLabel.setText(msg.getData());
                    if (user.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                    }

                    Platform.runLater(() -> {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        setGraphic(wrapper);
                    });
                }
            };
        }
    }

    public void addToChatContentListForBy(Message msg) {
        if (currentChatUser.equals(msg.getSendTo())) {
            chatContent.get(currentChatUser).add(msg);
            ObservableList<Message> chattingRecords = FXCollections.observableArrayList(
                chatContent.get(currentChatUser));
            chatContentList.setItems(chattingRecords);
        }
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

    public void MessagePrompt(Message msg) {
        Stage promptStage = new Stage();
        promptStage.initModality(Modality.APPLICATION_MODAL);
        promptStage.setTitle("Prompt");

        Label label = new Label(
            "Received message from " + msg.getSentBy() + ": ");
        label.getStyleClass().add("label");
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #333;");
        Label message = new Label(msg.getData());
        message.getStyleClass().add("label");
        message.setStyle("-fx-font-size: 16px; -fx-text-fill: #333;");

        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: #fee1b8; -fx-text-fill: #fff; -fx-font-weight: bold;");
        okButton.setOnAction(event -> promptStage.close());
        okButton.setPrefWidth(52);
        okButton.setPrefHeight(25);

        VBox vbox = new VBox(10, label, message, okButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setStyle(
            "-fx-background-color: #fff; -fx-border-color: #fee1b8; -fx-border-width: 3px;");

        Scene promptScene = new Scene(vbox, 300, 150);
        promptStage.setScene(promptScene);

        promptStage.initStyle(StageStyle.TRANSPARENT);
        promptStage.setResizable(false);

        promptStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            promptStage.close();
        }));
        timeline.play();
    }

    public void ServerDownPrompt() {
        Stage promptStage = new Stage();
        promptStage.initModality(Modality.APPLICATION_MODAL);
        promptStage.setTitle("Prompt");

        Label label = new Label("The server has been shut down.");
        label.getStyleClass().add("label");
        label.setStyle("-fx-font-size: 16px; -fx-text-fill: #333;");

        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: #fee1b8; -fx-text-fill: #fff; -fx-font-weight: bold;");
        okButton.setOnAction(event -> promptStage.close());
        okButton.setPrefWidth(52);
        okButton.setPrefHeight(25);

        VBox vbox = new VBox(10, label, okButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setStyle(
            "-fx-background-color: #fff; -fx-border-color: #fee1b8; -fx-border-width: 3px;");

        Scene promptScene = new Scene(vbox, 300, 150);
        promptStage.setScene(promptScene);

        promptStage.initStyle(StageStyle.TRANSPARENT);
        promptStage.setResizable(false);

        promptStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            promptStage.close();
        }));
        timeline.play();
    }
}
