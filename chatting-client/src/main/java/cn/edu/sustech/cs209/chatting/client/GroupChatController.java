package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.GroupMessage;
import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 */
public class GroupChatController implements Initializable {

    User user;
    @FXML
    ListView<User> groupChatUserList;
    @FXML
    ListView<GroupMessage> groupChatContentList;
    @FXML
    TextArea groupMessageArea;

    Logger logger = LoggerFactory.getLogger(GroupChatController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        groupChatUserList.setCellFactory(new UserCellFactory());
        groupChatContentList.setCellFactory(new GroupMessageCellFactory());
    }

    /**
     * .
     */
    public static class UserCellFactory implements Callback<ListView<User>, ListCell<User>> {

        @Override
        public ListCell<User> call(ListView<User> param) {
            return new ListCell<>() {

                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);

                    if (empty || Objects.isNull(user)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                    Text nameText = new Text();
                    Text statusText = new Text();

                    nameText.setFont(Font.font("Arial", 16));
                    statusText.setFont(Font.font("Arial", 12));
                    statusText.setFill(Color.GRAY);

                    nameText.setText(user.getName());
                    statusText.setText(user.getStatus().toString());
                    Circle statusCircle = new Circle(7);
                    statusCircle.setFill(Paint.valueOf(user.getStatus().getColor()));
                    HBox wrapper = new HBox();
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

    private class GroupMessageCellFactory implements
        Callback<ListView<GroupMessage>, ListCell<GroupMessage>> {

        @Override
        public ListCell<GroupMessage> call(ListView<GroupMessage> param) {
            return new ListCell<>() {


                @Override
                public void updateItem(GroupMessage msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label();

                    nameLabel.setMaxWidth(Double.MAX_VALUE);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-font-weight: bold;");

                    Label msgLabel = new Label();

                    msgLabel.setWrapText(true);
                    msgLabel.setStyle(
                        "-fx-background-color: #f4f4f4; -fx-background-radius: 10px; -fx-padding: 10px;");

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

    /**
     * .
     */
    public void showGroupChatView(ObservableList<User> groupChatUserList, Scene scene)
        throws IOException {
        Platform.runLater(() -> {
            Stage groupChatStage = new Stage();
            groupChatStage.setOnCloseRequest((WindowEvent e) -> {
                try {
                    Listener.leaveGroup(user);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            this.groupChatUserList.setItems(groupChatUserList);
            groupChatStage.setResizable(false);
            groupChatStage.setScene(scene);
            groupChatStage.show();
        });
    }

    /**
     * .
     */
    public void groupSendMessage() throws IOException {
        if (groupMessageArea.getText() != null) {
            String data = groupMessageArea.getText();
            GroupMessage msg = new GroupMessage(user, data);
            if (!groupMessageArea.getText().isEmpty()) {
                Listener.sendForGroup(msg);
                groupMessageArea.clear();
                logger.info("You have successfully sent a message to the group: " + msg.getData());
            }
        }
    }

    public void addToChatContentList(GroupMessage msg) {
        groupChatContentList.getItems().add(msg);
    }

    public void removeLeavedUser(User leaveUser) {
        Platform.runLater(() -> groupChatUserList.getItems().remove(leaveUser));
    }
}
