package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import layers.ProtocolStack;
import layers.SerializationException;
import layers.apl.Message;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
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
    public Circle statusIcon;
    public Label statusText;

    private ProtocolStack protocolStack;
    private Status status;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        protocolStack = (ProtocolStack) data;

        WebEngine engine = webView.getEngine();
        engine.loadContent(getHtmlPage());

        statusIcon.setFill(Status.NotConnected.toColor());
        statusText.setText(Status.NotConnected.toString());

        protocolStack.getPhy().subscribeConnectionStatusChanged(this::updateStatus);

        stage.setOnCloseRequest(e -> {
            Action action = Dialogs.create()
                                .owner(stage)
                                .title("ComChat")
                                .masthead("Confirmation")
                                .message("Do you really want to exit?")
                                .showConfirm();

            if (action == Dialog.Actions.YES) {
                if (status == Status.Connected)
                    protocolStack.getPhy().disconnect();
            }
            else
                e.consume();
        });
    }

    private void updateStatus(Boolean connected) {
        status = Status.fromBoolean(connected);
        statusIcon.setFill(status.toColor());
        statusText.setText(status.toString());
        sendButton.setDisable(!connected);
    }

    public void sendClick(ActionEvent actionEvent) {
        String message = inputField.getText();

        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();

        Node body = document.getElementsByTagName("body").item(0);
        Element div = engine.getDocument().createElement("div");
        Text text = engine.getDocument().createTextNode(message);

        Element b = webView.getEngine().getDocument().createElement("b");
        b.setTextContent("Вася: ");

        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);

        //send
        try {
            protocolStack.getApl().send(Message.Type.Msg, message);
        } catch (SerializationException | IOException e) {
            e.printStackTrace();
        }

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

            statusIcon.setFill(Status.Connected.toColor());
            statusText.setText(Status.Connected.toString());
        }
        else {
            Dialogs.create()
                .owner(stage)
                .title("ComChat")
                .masthead("Information")
                .message("Connection cancelled")
                .showInformation();
        }
    }

    public void onMenuDisconnect(ActionEvent event) {
        if (status == Status.Connected)
            protocolStack.getPhy().disconnect();
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

    public void onMenuAbout(ActionEvent event) {
        Dialogs.create()
                .owner(stage)
                .title("ComChat")
                .masthead("About")
                .message("BMSTU course project.\nCOM-port based chat for 2 persons.\n\nAuthors:\nLeontiev Aleksey - Application Layer and GUI\nLatkin Igor - Physical Layer\nKornukov Nikita - Data Link Layer\n\nProject's home:\nhttps://github.com/tech-team/comchat")
                .showInformation();
    }
}
