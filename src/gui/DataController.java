package gui;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public abstract class DataController {
    protected Object data;
    protected DialogResult result = DialogResult.CLOSED;
    protected Stage stage;

    public void initWithData(Stage stage, Object data) {
        this.stage = stage;
        this.data = data;
    }

    public DialogResult getResult() {
        return result;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void close() {
        stage.close();
    }
}
