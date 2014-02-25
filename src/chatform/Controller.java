package chatform;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import layers.ApplicationLayer;

public class Controller {
    public Button sendButton;
    public WebView webView;
    public TextArea inputField;

    private ApplicationLayer appLevel = null;

    public void sendClick(ActionEvent actionEvent) {

        //TODO: replace apostrophes and tags with their html-save versions
        webView.getEngine().executeScript(
                "document.write('<b>Вася: </b>" + inputField.getText() + "<br>');");
        inputField.setText("");
    }
}
