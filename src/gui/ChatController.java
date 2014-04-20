package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import layers.ProtocolStack;
import org.controlsfx.dialog.Dialogs;
import org.w3c.dom.Document;
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

    private ProtocolStack protocolStack;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        protocolStack = (ProtocolStack) data;

        WebEngine engine = webView.getEngine();
        engine.loadContent(getHtmlPage());
    }

    public void sendClick(ActionEvent actionEvent) {
        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();

        Node body = document.getElementsByTagName("body").item(0);
        Element div = engine.getDocument().createElement("div");
        Text text = engine.getDocument().createTextNode("ололо");

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

        connectionStage = new DataStage((DataController) loader.getController(), protocolStack);

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
            Dialogs.create()
                .owner(stage)
                .title("ComChat")
                .masthead("Information")
                .message("Successfully connected")
                .showInformation();
        } else {
            Dialogs.create()
                .owner(stage)
                .title("ComChat")
                .masthead("Information")
                .message("Connection cancelled")
                .showInformation();
        }
    }

    private String getHtmlPage() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<body>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
