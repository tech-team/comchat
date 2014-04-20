package gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import layers.phy.ComPort;
import layers.phy.ComPortSettings;
import layers.phy.IComPort;

public class ConnectionController extends DataController {
    public GridPane layout;
    public ComboBox<String> comPorts;
    public ComboBox<Integer> baudRate;
    public ComboBox<Integer> dataBits;
    public ComboBox<Integer> stopBits;
    public ComboBox<Integer> parityCheck;

    private IComPort comPort;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        comPort = (IComPort) data;

        comPorts.setItems(FXCollections.observableArrayList(ComPort.getAvailablePorts()));
        baudRate.setItems(FXCollections.observableArrayList(ComPort.getAvailableBaudRates()));
        dataBits.setItems(FXCollections.observableArrayList(ComPort.getAvailableDataBits()));
        stopBits.setItems(FXCollections.observableArrayList(ComPort.getAvailableStopBits()));
        parityCheck.setItems(FXCollections.observableArrayList(ComPort.getAvailableParity()));

        comPorts.setValue(ComPort.getDefaultPort());
        baudRate.setValue(ComPort.getDefaultBaudRate());
        dataBits.setValue(ComPort.getDefaultDataBits());
        stopBits.setValue(ComPort.getDefaultStopBits());
        parityCheck.setValue(ComPort.getDefaultParity());
    }


    public void onConnect(ActionEvent event) {
        try {
            comPort.connect(new ComPortSettings(comPorts.getValue(),
                                                baudRate.getValue(),
                                                dataBits.getValue(),
                                                stopBits.getValue(),
                                                parityCheck.getValue()));
            result = DialogResult.OK;
        }
        catch(Exception e) {
            //TODO: create app-specific exceptions or use existed ones
        }

        this.close();
    }
}
