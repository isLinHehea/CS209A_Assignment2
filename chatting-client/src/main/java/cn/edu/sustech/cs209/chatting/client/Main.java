package cn.edu.sustech.cs209.chatting.client;

import java.net.URISyntaxException;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException, URISyntaxException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("views/LoginView.fxml"));
        primaryStage.getIcons().add(new Image(
            Objects.requireNonNull(getClass().getResource("images/img.png")).toURI().toString()));
        primaryStage.setScene(new Scene(fxmlLoader.load()));
        primaryStage.setTitle("SUSTech Chatting Client Developed By IsLinHehea");
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());
    }
}
