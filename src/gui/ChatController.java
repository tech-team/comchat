package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import layers.ApplicationLayer;

import java.io.IOException;

public class ChatController {
    public Button sendButton;
    public WebView webView;
    public TextArea inputField;
    public Stage connectionStage = new Stage();
    public VBox layout;

    private ApplicationLayer appLevel = null;

    public void sendClick(ActionEvent actionEvent) {

        //TODO: replace apostrophes and tags with their html-save versions
        webView.getEngine().executeScript(
                "document.write('<b>Вася: </b>" + inputField.getText() + "<br>');");
        inputField.setText("");
    }

    public void onMenuConnect(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(Main.class.getResource("/gui/templates/connection.fxml"));

        connectionStage.setTitle("Connection");
        Scene conScene = new Scene(root);
        conScene.getStylesheets().add("/gui/css/connection.css");
        connectionStage.setScene(conScene);
        connectionStage.setResizable(false);
        connectionStage.initModality(Modality.WINDOW_MODAL);
        connectionStage.initOwner(layout.getScene().getWindow());
        connectionStage.initStyle(StageStyle.UTILITY);

        connectionStage.showAndWait();
    }
}
