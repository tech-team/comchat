package gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

    private String localUser = "Вася";
    private String remoteUser = "Петя";

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
            } else
                e.consume();
        });

        protocolStack.getApl().subscribeToReceive(this::receive);
    }

    private void updateStatus(Boolean connected) {
        status = Status.fromBoolean(connected);
        statusIcon.setFill(status.toColor());
        statusText.setText(status.toString());
        sendButton.setDisable(!connected);
    }

    private void addUserMessage(String author, String message) {
        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();
        Node body = document.getElementsByTagName("BODY").item(0);
        Element div = document.createElement("div");
        Text text = document.createTextNode(message);

        Element b = document.createElement("b");
        b.setTextContent(author + ": ");

        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);
    }

    private void addSystemMessage(MessageLevel level, String message) {
        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();

        Node body = document.getElementsByTagName("BODY").item(0);
        Element div = document.createElement("div");
        div.setAttribute("style", "color: " + level.toHtmlColor());

        Text text = document.createTextNode(message);

        Element b = document.createElement("b");
        b.setTextContent("[" + level.toString() + "]" + " System message: ");

        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);
    }

    private void send() {
        String message = inputField.getText();

        try {
            protocolStack.getApl().send(Message.Type.Msg, message);
        } catch (SerializationException | IOException e) { //TODO: IOException should be gone
            e.printStackTrace();
        }

        addUserMessage(localUser, message);
        inputField.clear();
    }

    private void receive(Message message) {
        //JavaFX UI thread synchronization
        Platform.runLater(() -> {
            if (message.getType() == Message.Type.Msg)
                addUserMessage(remoteUser, message.getMsg());
            else //TODO: should react differently on different message types
                addSystemMessage(MessageLevel.Info, message.getType().name());//Update UI here
        });
    }

    public void sendClick(ActionEvent event) {
        send();
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
        return "<html><head></head><body></body></html>";
    }

    public void onMenuAbout(ActionEvent event) {
        Dialogs.create()
                .owner(stage)
                .title("ComChat")
                .masthead("About")
                .message("BMSTU course project.\nCOM-port based chat for 2 persons.\n\nAuthors:\nLeontiev Aleksey - Application Layer and GUI\nLatkin Igor - Physical Layer\nKornukov Nikita - Data Link Layer\n\nProject's home:\nhttps://github.com/tech-team/comchat")
                .showInformation();
    }

    public void onKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isControlDown()) {
            if (status == Status.Connected)
                send();
            else
                Dialogs.create()
                        .owner(stage)
                        .title("ComChat")
                        .masthead("Error")
                        .message("You should connect first.\nUse Connection -> Connect.")
                        .showError();
        }
    }
}
