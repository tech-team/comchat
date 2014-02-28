package gui;
import com.sun.corba.se.spi.activation._InitialNameServiceImplBase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    //private static Main instance = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //instance = this;

        Parent root = FXMLLoader.load(getClass().getResource("gui/templates/chat.fxml"));
        primaryStage.setTitle("ComChat v0.1 alpha");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    /*public static URL getResource(String res) {
        return instance.getClass().getResource(res);
    }*/

    public static void main(String[] args) {
        launch(args);
    }
}
