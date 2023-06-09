package cn.edu.sustech.cs209.chatting.client;

import static cn.edu.sustech.cs209.chatting.common.Status.ONLINE;

import cn.edu.sustech.cs209.chatting.common.User;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 */
public class LoginController implements Initializable {

    @FXML
    public TextField RegisterUserName;
    @FXML
    public PasswordField RegisterPassword;
    @FXML
    public Pane Register;
    @FXML
    public TextField HostName;
    @FXML
    public TextField Port;
    @FXML
    private Pane Login;
    @FXML
    private TextField UserName;
    @FXML
    private PasswordField Password;
    private Scene scene;
    private static LoginController instance;
    private Statement stmt = null;

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    private Pane Pane;

    public LoginController() {
        instance = this;
    }

    public static LoginController getInstance() {
        return instance;
    }

    /**
     * .
     */
    public void loginButtonAction() throws IOException, SQLException {
        String username = UserName.getText();
        String password = Password.getText();

        String sql =
            "select * from CHAT where USERNAME = '" + username + "' and PASSWORD = '" + password
                + "';";
        ResultSet rs = stmt.executeQuery(sql);
        if (!rs.next()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Password Error");
            alert.setHeaderText("Wrong Password!");
            alert.setContentText("Please re-enter.");
            alert.showAndWait();

            UserName.clear();
            Password.clear();
        } else {
            User user = new User(username, password, ONLINE);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("views/ChatView.fxml"));
            this.scene = new Scene(fxmlLoader.load());
            ChatController chatController = fxmlLoader.getController();
            Listener listener = new Listener(HostName.getText(), Integer.parseInt(Port.getText()),
                user, chatController);
            Thread x = new Thread(listener);
            x.start();
        }
    }

    /**
     * .
     */
    public void registerButtonAction() throws SQLException {
        String username = RegisterUserName.getText();
        String password = RegisterPassword.getText();
        String sql =
            "select * from CHAT where USERNAME = '" + username + "';";
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("UserName Conflict");
            alert.setHeaderText("Duplicate UserName!");
            alert.setContentText("Registration failure, please re-enter.");
            alert.showAndWait();

            RegisterUserName.clear();
            RegisterPassword.clear();
        } else {
            String sqlRegister =
                "insert into CHAT (USERNAME, PASSWORD) values ('" + username + "', '" + password
                    + "')";
            stmt.executeUpdate(sqlRegister);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText("Successfully register~");
            Button btnOk = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            btnOk.setOnAction(event -> {
                try {
                    returnLoginButtonAction();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            alert.show();
        }
    }

    /**
     * .
     */
    public void returnLoginButtonAction() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("views/LoginView.fxml"));
        Pane = Login;
        this.scene = new Scene(fxmlLoader.load());
        Platform.runLater(() -> {
            Stage stage = (Stage) RegisterUserName.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.show();
        });
    }

    /**
     * .
     */
    public void showScene() throws IOException {
        Platform.runLater(() -> {
            Stage stage = (Stage) UserName.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        });
    }

    /**
     * .
     */
    public void showRegisterView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("views/RegisterView.fxml"));
        Pane = Register;
        this.scene = new Scene(fxmlLoader.load());
        Platform.runLater(() -> {
            Stage stage = (Stage) UserName.getScene().getWindow();
            stage.setOnCloseRequest((WindowEvent e) -> {
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(this.scene);
            stage.show();
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (Login == null) {
            Pane = Register;
        } else {
            Pane = Login;
        }
        animation(Pane);
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager
                .getConnection("jdbc:postgresql://localhost:5432/postgres",
                    "postgres", "123456");
            stmt = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Open database successfully!");
    }

    /**
     * .
     */
    public void animation(Pane pane) {
        int numberOfSquares = 52;
        while (numberOfSquares > 0) {
            generateAnimation(pane);
            numberOfSquares--;
        }
    }

    /**
     * .
     */
    public void generateAnimation(Pane pane) {
        Random rand = new Random();
        int sizeOfSqaure = rand.nextInt(50) + 1;
        int startX = rand.nextInt(500);
        int startY = rand.nextInt(350);
        int direction = rand.nextInt(5) + 1;

        KeyValue moveX = null;
        KeyValue moveY = null;
        Rectangle r1 = null;

        switch (direction) {
            case 1 -> {
                // MOVE LEFT TO RIGHT
                r1 = new Rectangle(0, startY, sizeOfSqaure, sizeOfSqaure);
                moveX = new KeyValue(r1.xProperty(), 350 - sizeOfSqaure);
            }
            case 2 -> {
                // MOVE TOP TO BOTTOM
                r1 = new Rectangle(startX, 0, sizeOfSqaure, sizeOfSqaure);
                moveY = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
            }
            case 3 -> {
                //MOVE RIGHT TO LEFT, BOTTOM TO TOP
                // MOVE LEFT TO RIGHT, TOP TO BOTTOM
                r1 = new Rectangle(startX, 0, sizeOfSqaure, sizeOfSqaure);
                moveX = new KeyValue(r1.xProperty(), 350 - sizeOfSqaure);
                moveY = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
            }
            case 4 -> {
                // MOVE BOTTOM TO TOP
                r1 = new Rectangle(startX, 420 - sizeOfSqaure, sizeOfSqaure, sizeOfSqaure);
                moveY = new KeyValue(r1.xProperty(), 0);
            }
            case 5 -> {
                // MOVE RIGHT TO LEFT
                r1 = new Rectangle(420 - sizeOfSqaure, startY, sizeOfSqaure, sizeOfSqaure);
                moveX = new KeyValue(r1.xProperty(), 0);
            }
            default -> System.out.println("default");
        }

        if (pane.equals(Login)) {
            r1.setFill(Color.web("#F89406"));
        } else {
            r1.setFill(Color.web("#069df8"));
        }
        r1.setOpacity(0.1);

        int speedOfSqaure = rand.nextInt(10) + 5;

        KeyFrame keyFrame = new KeyFrame(Duration.millis(speedOfSqaure * 1000), moveX,
            moveY);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
        pane.getChildren().add(pane.getChildren().size() - 1, r1);
    }
}
