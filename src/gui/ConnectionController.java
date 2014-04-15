package gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import layers.PhysicalLayer;

public class ConnectionController extends DataController {
    public GridPane layout;
    public ComboBox<String> comPort;
    public ComboBox<Integer> baudRate;
    public ComboBox<Integer> dataBits;
    public ComboBox<Integer> stopBits;
    public ComboBox<Integer> parityCheck;

    private PhysicalLayer physicalLayer;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        physicalLayer = (PhysicalLayer) data;

        comPort.setItems(FXCollections.observableArrayList(PhysicalLayer.getAvailablePorts()));
        baudRate.setItems(FXCollections.observableArrayList(PhysicalLayer.getAvailableBaudRates()));
        dataBits.setItems(FXCollections.observableArrayList(PhysicalLayer.getAvailableDataBits()));
        stopBits.setItems(FXCollections.observableArrayList(PhysicalLayer.getAvailableStopBits()));
        parityCheck.setItems(FXCollections.observableArrayList(PhysicalLayer.getAvailableParity()));

        comPort.setValue(PhysicalLayer.getDefaultPort());
        baudRate.setValue(PhysicalLayer.getDefaultBaudRate());
        dataBits.setValue(PhysicalLayer.getDefaultDataBits());
        stopBits.setValue(PhysicalLayer.getDefaultStopBits());
        parityCheck.setValue(PhysicalLayer.getDefaultParity());
    }


    public void onConnect(ActionEvent event) {
        physicalLayer.setSerialPortParams(baudRate.getValue(), dataBits.getValue(), stopBits.getValue(), parityCheck.getValue());

        try {
            physicalLayer.connect(comPort.getValue());
            result = DialogResult.OK;
        }
        catch(Exception e) {
            //TODO: create app-specific exceptions or use existed ones
        }

        this.close();
    }
}
