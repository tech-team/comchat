package gui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import layers.ProtocolStack;
import layers.phy.PhysicalLayer;
import org.controlsfx.dialog.Dialogs;

import java.util.HashMap;
import java.util.Map;

public class ConnectionController extends DataController {
    public GridPane layout;
    public ComboBox<String> comPort;
    public ComboBox<Integer> baudRate;
    public ComboBox<Integer> dataBits;
    public ComboBox<Integer> stopBits;
    public ComboBox<Integer> parityCheck;

    private ProtocolStack protocolStack;

    @Override
    public void initWithData(Stage stage, Object data) {
        super.initWithData(stage, data);
        protocolStack = (ProtocolStack) data;

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
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("port", comPort.getValue());
            //TODO: fill map with all the settings

            protocolStack.getPhy().connect(settings);

            /*protocolStack.getPhy().connect(new ComPortSettings(comPorts.getValue(),
                    baudRate.getValue(),
                    dataBits.getValue(),
                    stopBits.getValue(),
                    parityCheck.getValue()));*/
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
