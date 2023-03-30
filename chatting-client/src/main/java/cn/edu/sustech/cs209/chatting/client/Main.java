package cn.edu.sustech.cs209.chatting.client;

import java.net.URISyntaxException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import jdk.incubator.vector.VectorOperators.Test;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.getIcons().add(new Image(
            Objects.requireNonNull(getClass().getResource("img.png")).toURI().toString()));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client By IsLinHehea");
        stage.show();
    }
}
