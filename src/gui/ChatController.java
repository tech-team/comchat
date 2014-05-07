package gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import layers.ProtocolStack;
import layers.apl.Message;
import org.controlsfx.dialog.Dialogs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.HashMap;

public class ChatController extends DataController {
    @FXML private Button sendButton;
    @FXML private WebView webView;
    @FXML private TextArea inputField;
    @FXML private VBox layout;
    @FXML private Circle statusIcon;
    @FXML private Label statusText;
    @FXML private Circle ctsIcon;

    public static final String PROGRAM_NAME = "ComChat";
    public static final String PROGRAM_VERSION = "v0.1 alpha";
    public static final boolean DEBUG = false;

    private ProtocolStack protocolStack;
    private Status status;

    private String localUser = null;
    private String remoteUser = null;
    private boolean isAuthorized = false;

    private static final String messageSent = "[×] ";
    private static final String messageReceived = "[«] ";
    private static final String messageAck = "[»] ";

    private boolean isConnected = false;

    private Integer messageId = 0;

    private boolean isClosing = false;

    private HashMap<Integer, Integer> messageIdToHtmlId = new HashMap<>();

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        protocolStack = (ProtocolStack) data;

        WebEngine engine = webView.getEngine();
        engine.loadContent(getHtmlPage());

        statusIcon.setFill(Status.NotConnected.toColor());
        statusText.setText(Status.NotConnected.toString());

        protocolStack.getPhy().subscribeConnectionStatusChanged(this::updateStatus);
        protocolStack.getPhy().subscribeCompanionConnectedChanged(this::updateCompanionStatus);
        protocolStack.getPhy().subscribeSendingAvailableChanged(this::updateCTS);

        stage.setOnCloseRequest(e -> {
            isClosing = true;
            if (status != Status.NotConnected)
                protocolStack.getApl().disconnect();
        });

        protocolStack.getApl().subscribeToReceive(this::receive);

        Platform.runLater(this::showConnectionDialog);
    }

    private void updateStatus(boolean connected) {
        this.isConnected = connected;
        Status newStatus = Status.fromBoolean(connected);
        if (status == newStatus)
            return;

        Platform.runLater(() -> {
            status = newStatus;
            statusIcon.setFill(status.toColor());
            statusText.setText(status.toString());
            //sendButton.setDisable(!connected);

            if (status == Status.NotConnected && !isClosing) {
                isAuthorized = false;
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

    private void updateCompanionStatus(boolean connected) {
        if (!this.isConnected)
            return;

        Status newStatus = connected ? Status.Chatting : Status.Connected;

        if (status != newStatus) {
            //if we are connected and connection form closed (overwise Auth will be sent after form closing)
            if (connected && !isAuthorized && localUser != null) {
                protocolStack.getApl().send(Message.Type.Auth, localUser);
                isAuthorized = true;
            }
            else if (status == Status.Chatting) {
                addSystemMessage(MessageLevel.Info, "Remote user disconnected, waiting for another companion...");
                isAuthorized = false;
            }

            Platform.runLater(() -> {
                status = newStatus;
                statusIcon.setFill(status.toColor());
                statusText.setText(status.toString());
                sendButton.setDisable(!connected);

                if (status != Status.Chatting)
                    ctsIcon.setFill(Color.RED);
            });
        }
    }

    private void updateCTS(boolean CTS) {
        Platform.runLater(() -> {
            if (status != Status.Chatting) {
                ctsIcon.setFill(Color.RED);
                return;
            }

            if (CTS)
                ctsIcon.setFill(Color.YELLOWGREEN);
            else
                ctsIcon.setFill(Color.RED);
        });
    }

    private void addUserMessage(String author, String message) {
        addUserMessage(author, message, 0);
    }

    private void addUserMessage(String author, String message, Integer id) {
        System.out.println(author + ": " + message);

        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();
        Node body = document.getElementsByTagName("BODY").item(0);
        Element div = document.createElement("div");

        messageIdToHtmlId.put(id, messageId);
        div.setAttribute("id", messageId.toString());
        div.setAttribute("data-message-id", id.toString());

        Text text = document.createTextNode(message);

        Element mark = document.createElement("b");
        if (id != 0)
            mark.setTextContent(messageSent);
        else
            mark.setTextContent(messageReceived);

        Element b = document.createElement("b");
        b.setTextContent(author + ": ");

        div.appendChild(mark);
        div.appendChild(b);
        div.appendChild(text);

        body.appendChild(div);
        scrollChatToId(messageId++);
    }

    private void addSystemMessage(MessageLevel level, String message) {
        System.out.println(level.toString() + ": " + message);
        if (level == MessageLevel.Debug && !DEBUG)
            return;

        Platform.runLater(() -> {
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
        });
    }

    private void send() {
        String message = inputField.getText();

        if (message.isEmpty())
            return;

        int id = protocolStack.getApl().send(Message.Type.Msg, message);

        addUserMessage(localUser, message, id);
        inputField.clear();
    }

    private void receive(Message message) {
        //JavaFX UI thread synchronization
        Platform.runLater(() -> {
            addSystemMessage(MessageLevel.Debug, "Message received: " + message.getType().name());

            switch (message.getType()) {
                case Msg:
                    addUserMessage(remoteUser, message.getMsg());
                    protocolStack.getApl().send(Message.Type.Ack,
                            Integer.toString(message.getId()));
                    break;
                case Auth:
                    remoteUser = message.getMsg();
                    addSystemMessage(MessageLevel.Info, "Remote user connected: " + message.getMsg());
                    protocolStack.getApl().handshakeFinished();
                    break;
                case Ack:
                    int id = Integer.parseInt(message.getMsg());
                    markMessage(id);
                    break;
                default:
                    throw new NotImplementedException();
            }
        });
    }

    private void markMessage(int id) {
        WebEngine engine = webView.getEngine();
        Document document = engine.getDocument();

        Integer htmlId = messageIdToHtmlId.get(id);
        if (htmlId == null)
            return;

        Element div = document.getElementById(htmlId.toString());
        Element mark = (Element) div.getFirstChild();

        mark.setTextContent(messageAck);
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

            DataStage connectionStage = new DataStage(loader.getController(), protocolStack);

            connectionStage.setTitle("Connection");
            final double rem = javafx.scene.text.Font.getDefault().getSize() / 13;
            Scene conScene = new Scene(root, 400.0*rem, 205.0*rem);
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

        if (!isAuthorized) {
            isAuthorized = true;
            protocolStack.getApl().send(Message.Type.Auth, localUser);
        }

        protocolStack.getApl().subscribeOnError(this::onError);
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
        if (status != Status.NotConnected)
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
            if (status != Status.NotConnected)
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
