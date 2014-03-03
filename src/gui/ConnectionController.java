package gui;

import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import layers.PhysicalLayer;

public class ConnectionController extends DataController {
    public GridPane layout;
    public ComboBox comPort;
    public ComboBox baudRate;
    public ComboBox dataBits;
    public ComboBox stopBits;
    public ComboBox parityCheck;

    private PhysicalLayer physicalLayer;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        physicalLayer = (PhysicalLayer) data;
        //TODO: get COM port list from physicalLayer
        //comPort... = physicalLayer.getPortList();
    }


    public void onConnect(ActionEvent event) {
        //TODO: convert String params to enum/integer value
        //physicalLayer.connect(); //or setParams()
        result = DialogResult.OK;

        this.close();
    }
}
