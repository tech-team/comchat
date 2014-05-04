package gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import layers.ProtocolStack;
import layers.exceptions.ConnectionException;
import layers.phy.ComPort;
import layers.phy.settings.comport_settings.ComPortSettings;
import layers.phy.settings.comport_settings.DataBitsEnum;
import layers.phy.settings.comport_settings.ParityEnum;
import layers.phy.settings.comport_settings.StopBitsEnum;
import org.controlsfx.dialog.Dialogs;

public class ConnectionController extends DataController {
    @FXML private TextField userName;
    @FXML private ComboBox<String> comPort;
    @FXML private ComboBox<Integer> baudRate;
    @FXML private ComboBox<String> dataBits;
    @FXML private ComboBox<String> stopBits;
    @FXML private ComboBox<String> parityCheck;

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
        if (comPort.getValue().isEmpty())
        {
            Dialogs.create()
                    .owner(stage)
                    .title(ChatController.PROGRAM_NAME)
                    .masthead("Error")
                    .message("Please, select COM port.\nIf you do not see any listed, press Refresh button.")
                    .showError();

            return;
        }

        if (userName.getText().isEmpty())
        {
            Dialogs.create()
                    .owner(stage)
                    .title(ChatController.PROGRAM_NAME)
                    .masthead("Error")
                    .message("Please, input your username.")
                    .showError();

            return;
        }

        try {
            protocolStack.getApl().connect(
                    new ComPortSettings(comPort.getValue(),
                            baudRate.getValue(),
                            DataBitsEnum.fromString(dataBits.getValue()).toDataBits(),
                            StopBitsEnum.fromString(stopBits.getValue()).toStopBits(),
                            ParityEnum.fromString(parityCheck.getValue()).toParity()));

            result = DialogResult.OK;
            resultData = userName.getText();
            this.close();
        }
        catch(ConnectionException e) {
            String message = "Unable to connect with these settings";
            Exception withMessage = new Exception(message, e);

            Platform.runLater(() -> Dialogs.create()
                    .owner(stage)
                    .title("ComChat")
                    .masthead("Error")
                    .message(message) //well, that has no effect for exception dialog unfortunately
                    .showException(withMessage));
        }
    }

    public void onRefresh(ActionEvent event) {
        comPort.setItems(FXCollections.observableArrayList(ComPort.getAvailablePorts(true)));
        comPort.setValue(ComPort.getDefaultPort());
    }
}
