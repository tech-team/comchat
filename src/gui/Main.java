package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import layers.ProtocolStack;
import layers.apl.ApplicationLayer;
import layers.dll.DataLinkLayer;
import layers.phy.ComPort;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ProtocolStack stack = new ProtocolStack(ApplicationLayer.class, DataLinkLayer.class, ComPort.class);

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/gui/templates/chat.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("ComChat v0.1 alpha");
        primaryStage.setScene(new Scene(root, 600, 400));

        DataController controller = loader.getController();
        controller.initWithData(primaryStage, stack);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
