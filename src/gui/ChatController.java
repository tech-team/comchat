package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import layers.ApplicationLayer;

import java.io.IOException;

public class ChatController {
    public Button sendButton;
    public WebView webView;
    public TextArea inputField;
    public Stage connectionStage;

    private ApplicationLayer appLevel = null;

    public void sendClick(ActionEvent actionEvent) {

        //TODO: replace apostrophes and tags with their html-save versions
        webView.getEngine().executeScript(
                "document.write('<b>Вася: </b>" + inputField.getText() + "<br>');");
        inputField.setText("");
    }

    public void onMenuConnect(ActionEvent actionEvent) throws IOException {
        /*Parent root = FXMLLoader.load(Main.getResource("gui/templates/connection.fxml"));
        connectionStage.setTitle("ComChat v0.1 alpha - Connection");
        connectionStage.setScene(new Scene(root, 300, 200));
        connectionStage.show();*/
    }
}
