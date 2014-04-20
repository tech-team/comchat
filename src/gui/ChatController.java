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
import layers.dll.DataLinkLayer;
import layers.dll.IDataLinkLayer;
import layers.phy.ComPort;
import layers.phy.IComPort;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.io.IOException;

public class ChatController extends DataController {
    public Button sendButton;
    public WebView webView;
    public TextArea inputField;
    public DataStage connectionStage;
    public VBox layout;

    private IDataLinkLayer dataLinkLayer = new DataLinkLayer();
    private IComPort comPort = new ComPort(dataLinkLayer);

    public void sendClick(ActionEvent actionEvent) {
        Node body = webView.getEngine().getDocument().getElementsByTagName("body").item(0);
        Element div = webView.getEngine().getDocument().createElement("div");
        Text text = webView.getEngine().getDocument().createTextNode("ололо");

        Element b = webView.getEngine().getDocument().createElement("b");
        b.setTextContent("Вася: ");

        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);

        inputField.clear();
    }

    public void onMenuConnect(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/gui/templates/connection.fxml"));

        Parent root = (Parent) loader.load();

        connectionStage = new DataStage((DataController) loader.getController(), comPort);

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