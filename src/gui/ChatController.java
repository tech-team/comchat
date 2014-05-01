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
import layers.apl.Message;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

public class ChatController extends DataController {
    public Button sendButton;
    public WebView webView;
    public TextArea inputField;
    public DataStage connectionStage;
    public VBox layout;
    public Circle statusIcon;
    public Label statusText;

    public static final String PROGRAM_NAME = "ComChat";
    public static final String PROGRAM_VERSION = "v0.1 alpha";

    private ProtocolStack protocolStack;
    private Status status;

    private String localUser = "undefined";
    private String remoteUser = "undefined";

    private Integer messageId = 0;

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
                    .title(PROGRAM_NAME)
                    .masthead("Confirmation")
                    .message("Do you really want to exit?")
                    .showConfirm();

            if (action == Dialog.Actions.YES) {
                if (status == Status.Connected)
                    protocolStack.getApl().disconnect();
            } else
                e.consume();
        });

        protocolStack.getApl().subscribeToReceive(this::receive);

        Platform.runLater(this::showConnectionDialog);
    }

    private void updateStatus(boolean connected) {
        Status newStatus = Status.fromBoolean(connected);
        if (status == newStatus)
            return;

        Platform.runLater(() -> {
            status = newStatus;
            statusIcon.setFill(status.toColor());
            statusText.setText(status.toString());
            sendButton.setDisable(!connected);

            if (status == Status.NotConnected) {
                protocolStack.getApl().disconnect();
                addSystemMessage(MessageLevel.Info, "Disconnected");
                Dialogs.create()
                        .owner(stage)
                        .title(PROGRAM_NAME)
                        .masthead("Information")
                        .message("Disconnected")
                        .showInformation();
            }
        });
    }

    private void addUserMessage(String author, String message) {
        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();
        Node body = document.getElementsByTagName("BODY").item(0);
        Element div = document.createElement("div");
        div.setAttribute("id", messageId.toString());

        Text text = document.createTextNode(message);

        Element b = document.createElement("b");
        b.setTextContent(author + ": ");

        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);
        scrollChatToId(messageId++);
    }

    private void addSystemMessage(MessageLevel level, String message) {
        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();

        Node body = document.getElementsByTagName("BODY").item(0);
        Element div = document.createElement("div");
        div.setAttribute("id", messageId.toString());

        div.setAttribute("style", "color: " + level.toHtmlColor());

        Text text = document.createTextNode(message);

        Element b = document.createElement("b");
        b.setTextContent("[" + level.toString() + "]" + " System message: ");

        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);
        scrollChatToId(messageId++);
    }

    private void send() {
        String message = inputField.getText();

        protocolStack.getApl().send(Message.Type.Msg, message);

        addUserMessage(localUser, message);
        inputField.clear();
    }

    private void receive(Message message) {
        //JavaFX UI thread synchronization
        Platform.runLater(() -> {
            addSystemMessage(MessageLevel.Debug, "Message received: " + message.getType().name());

            switch (message.getType()) {
                case Msg:
                    addUserMessage(remoteUser, message.getMsg());
                    break;
                case Auth:
                    remoteUser = message.getMsg();
                    addSystemMessage(MessageLevel.Info, "Remote user connected: " + message.getMsg());
                    protocolStack.getApl().handshakeFinished();
                    //TODO: enable "sendability"
                    break;
                case Ack:
                    //TODO: is it necessary?
                    break;
                case Term:
                    addSystemMessage(MessageLevel.Info, "Termination requested from remote user");
//                    protocolStack.getApl().send(Message.Type.TermAck, ""); // TODO: not sure if we do not need to send any data
                    break;
                case TermAck:
                    //TODO: interface though all the layers?
                    addSystemMessage(MessageLevel.Info, "Termination confirmed");
                    protocolStack.getApl().disconnect();
                    addSystemMessage(MessageLevel.Info, "Disconnected");
                    break;
                default:
                    throw new NotImplementedException();
            }
        });
    }

    public void sendClick(ActionEvent event) {
        send();
    }

    public void onMenuConnect(ActionEvent event) throws IOException {
        showConnectionDialog();
    }

    private void showConnectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/gui/templates/connection.fxml"));

            Parent root = loader.load();

            connectionStage = new DataStage(loader.getController(), protocolStack);

            connectionStage.setTitle("Connection");
            final double rem = javafx.scene.text.Font.getDefault().getSize() / 13;
            Scene conScene = new Scene(root, 400.0*rem, 205.0*rem);
            conScene.getStylesheets().add("/gui/css/connection.css");
            connectionStage.setScene(conScene);
            connectionStage.setResizable(false);
            connectionStage.initModality(Modality.WINDOW_MODAL);
            connectionStage.initOwner(layout.getScene().getWindow());
            connectionStage.initStyle(StageStyle.UTILITY);
            connectionStage.showAndWait();

            if (connectionStage.getResult() == DialogResult.OK) {
                localUser = (String) connectionStage.getResultData();
                onConnect();
            } else {
                Dialogs.create()
                        .owner(stage)
                        .title(PROGRAM_NAME)
                        .masthead("Information")
                        .message("Connection cancelled")
                        .showInformation();
            }
        }
        catch (IOException e) {
            Dialogs.create()
                    .owner(stage)
                    .title(PROGRAM_NAME)
                    .masthead("Error")
                    .showException(e);
        }
    }

    private void onConnect() {
        //not sure why runLater needed here, cause we are already in UI thread and webEngine already loaded
        Platform.runLater(() -> addSystemMessage(MessageLevel.Info, "Successfully connected"));

        protocolStack.getApl().subscribeOnError(this::onError);
        protocolStack.getApl().send(Message.Type.Auth, localUser);
    }

    private void onError(Exception e) {
        protocolStack.getApl().disconnect();

        addSystemMessage(MessageLevel.Error, "Connection lost");
        Dialogs.create()
                .owner(stage)
                .title(PROGRAM_NAME)
                .masthead("Error")
                .message("Connection lost")
                .showException(e);
    }

    public void onMenuDisconnect(ActionEvent event) {
        if (status == Status.Connected)
            protocolStack.getApl().disconnect();
    }

    private String getHtmlPage() {
        return "<html><head></head><body></body></html>";
    }

    public void onMenuAbout(ActionEvent event) {
        Dialogs.create()
                .owner(stage)
                .title(PROGRAM_NAME)
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
                        .title(PROGRAM_NAME)
                        .masthead("Error")
                        .message("You should connect first.\nUse Connection -> Connect.")
                        .showError();
        }
    }

    private void scrollChatToId(int id) {
        try {
            WebEngine engine = webView.getEngine();
            engine.executeScript("document.getElementById(" + id + ").scrollIntoView()");
        }
        catch (NullPointerException ignored) {}
    }
}
