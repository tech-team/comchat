package gui;
import javafx.stage.Stage;

public class DataStage extends Stage {
    private DataController controller;

    public DataStage(DataController controller, Object data) {
        this.controller = controller;
        controller.initWithData(this, data);
    }

    public DialogResult getResult() {
        return controller.getResult();
    }

    public DataController getController() {
        return controller;
    }
}
