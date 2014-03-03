package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import layers.ChannelLayer;
import layers.PhysicalLayer;

import java.io.IOException;

public class ChatController extends DataController {
    public Button sendButton;
    public WebView webView;
    public TextArea inputField;
    public DataStage connectionStage;
    public VBox layout;

    private ChannelLayer channelLayer = null;
    private PhysicalLayer physicalLayer = null;

    public void sendClick(ActionEvent actionEvent) {

        //TODO: replace apostrophes and tags with their html-save versions
        webView.getEngine().executeScript(
                "document.write('<b>Вася: </b>" + inputField.getText() + "<br>');");
        inputField.setText("");
    }

    public void onMenuConnect(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/gui/templates/connection.fxml"));

        Parent root = (Parent) loader.load();

        connectionStage = new DataStage((DataController) loader.getController(), physicalLayer);

        connectionStage.setTitle("Connection");
        Scene conScene = new Scene(root);
        conScene.getStylesheets().add("/gui/css/connection.css");
        connectionStage.setScene(conScene);
        connectionStage.setResizable(false);
        connectionStage.initModality(Modality.WINDOW_MODAL);
        connectionStage.initOwner(layout.getScene().getWindow());
        connectionStage.initStyle(StageStyle.UTILITY);
        connectionStage.showAndWait();

        if (connectionStage.getResult() == DialogResult.OK) {
            Dialogs.showInformationDialog(stage, "We have successfully connected!",
                    "Information Dialog", "Connection");
        } else {
            Dialogs.showInformationDialog(stage, "Connection cancelled!",
                    "Information Dialog", "Connection");
        }
    }
}
