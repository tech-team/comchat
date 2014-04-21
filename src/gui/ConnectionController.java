package gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import layers.ProtocolStack;
import layers.phy.ComPort;
import layers.phy.settings.comport_settings.ComPortSettings;
import layers.phy.settings.comport_settings.DataBitsEnum;
import layers.phy.settings.comport_settings.ParityEnum;
import layers.phy.settings.comport_settings.StopBitsEnum;
import org.controlsfx.dialog.Dialogs;

public class ConnectionController extends DataController {
    public GridPane layout;
    public ComboBox<String> comPort;
    public ComboBox<Integer> baudRate;
    public ComboBox<String> dataBits;
    public ComboBox<String> stopBits;
    public ComboBox<String> parityCheck;

    private ProtocolStack protocolStack;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        protocolStack = (ProtocolStack) data;

        comPort.setItems(FXCollections.observableArrayList(ComPort.getAvailablePorts()));
        baudRate.setItems(FXCollections.observableArrayList(ComPort.getAvailableBaudRates()));
        dataBits.setItems(FXCollections.observableArrayList(ComPort.getAvailableDataBits()));
        stopBits.setItems(FXCollections.observableArrayList(ComPort.getAvailableStopBits()));
        parityCheck.setItems(FXCollections.observableArrayList(ComPort.getAvailableParity()));

        comPort.setValue(ComPort.getDefaultPort());
        baudRate.setValue(ComPort.getDefaultBaudRate());
        dataBits.setValue(ComPort.getDefaultDataBits());
        stopBits.setValue(ComPort.getDefaultStopBits());
        parityCheck.setValue(ComPort.getDefaultParity());
    }


    public void onConnect(ActionEvent event) {
        try {
            protocolStack.getPhy().connect(new ComPortSettings(comPort.getValue(),
                                                               baudRate.getValue(),
                                                               DataBitsEnum.fromString(dataBits.getValue()).toDataBits(),
                                                               StopBitsEnum.fromString(stopBits.getValue()).toStopBits(),
                                                               ParityEnum.fromString(parityCheck.getValue()).toParity()));
            result = DialogResult.OK;
            this.close();
        }
        catch(Exception e) {
            Dialogs.create()
                .owner(stage)
                .title("ComChat")
                .masthead("Error")
                .message("Unable to connect with these settings")
                .showInformation();
        }
    }
}
