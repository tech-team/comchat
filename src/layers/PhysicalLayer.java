package layers;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class PhysicalLayer implements SerialPortEventListener {
    private static Logger LOGGER = Logger.getLogger("PhysicalLayerLogger");
    private static List<String> availablePorts;
    private static final String PORT_NAME = "ChatPort";
    private static final int TIME_OUT = 2000;
    private int baudRate = -1;
    private int dataBits = -1;
    private int stopBits = -1;
    private int parity = -1;


    private SerialPort serialPort;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean connected = false;

    public PhysicalLayer() {

    }

    public boolean isConnected() {
        return connected;
    }

    public static List<String> getAvailablePorts(boolean ignoreCache) {
        if (!ignoreCache && availablePorts != null)
            return availablePorts;

        availablePorts = new LinkedList<String>();

        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) portEnum.nextElement();

            if(port.getPortType() == CommPortIdentifier.PORT_SERIAL)
                availablePorts.add(port.getName());
        }

        return availablePorts;
    }

    public static List<String> getAvailablePorts() {
        return getAvailablePorts(false);
    }

    public static List<Integer> getAvailableBaudRates() {
        return new ArrayList<Integer>(
                asList(300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200));
    }

    public static List<Integer> getAvailableDataBits() {
        List<Integer> dataBits = new LinkedList<Integer>();
        dataBits.add(SerialPort.DATABITS_5);
        dataBits.add(SerialPort.DATABITS_6);
        dataBits.add(SerialPort.DATABITS_7);
        dataBits.add(SerialPort.DATABITS_8);
        return dataBits;
    }

    public static List<Integer> getAvailableStopBits() {
        List<Integer> stopBits = new LinkedList<Integer>();
        stopBits.add(SerialPort.STOPBITS_1);
        stopBits.add(SerialPort.STOPBITS_1_5);
        stopBits.add(SerialPort.STOPBITS_2);
        return stopBits;
    }

    public static List<Integer> getAvailableParity() {
        List<Integer> parity = new LinkedList<Integer>();
        parity.add(SerialPort.PARITY_EVEN);
        parity.add(SerialPort.PARITY_MARK);
        parity.add(SerialPort.PARITY_ODD);
        parity.add(SerialPort.PARITY_SPACE);
        parity.add(SerialPort.PARITY_NONE);
        return parity;
    }

    public static String getDefaultPort() {
        String port = "";
        List<String> ports = getAvailablePorts();
        if (!ports.isEmpty())
            port = ports.get(0);

        return port;
    }

    public static int getDefaultBaudRate() {
        return 9600;
    }

    public static int getDefaultDataBits() {
        return SerialPort.DATABITS_8;
    }

    public static int getDefaultStopBits() {
        return SerialPort.STOPBITS_1;
    }

    public static int getDefaultParity() {
        return SerialPort.PARITY_NONE;
    }

    public void setSerialPortParams(int baudRate, int dataBits, int stopBits, int parity) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public void connect(String port) throws NoSuchPortException {
        LOGGER.info("Connecting to port " + port);
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);

        try {
            serialPort = (SerialPort) portId.open(PORT_NAME, TIME_OUT);
            LOGGER.info("Port " + port + " opened successfully");

            if (checkSerialPortParams()) {
                serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            }
            else {
                LOGGER.severe("SerialPort params are incorrect");
            }

            outStream = serialPort.getOutputStream();
            inStream = serialPort.getInputStream();

            connected = true;

        } catch (PortInUseException e) {
            LOGGER.warning("Port " + port + " is already in use");
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.severe("Error while opening streams for serial port");
            disconnect();
        }

        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnOutputEmpty(true);
        } catch (TooManyListenersException e) {
            LOGGER.severe("Too many listeners");
            disconnect();
        }
    }

    public void disconnect() {
        if (serialPort != null) {
            try {
                outStream.close();
                inStream.close();
            } catch (IOException ignored) {
            }

            serialPort.close();

            LOGGER.info("Port " + serialPort.getName() + " closed");
            outStream = null;
            inStream = null;
            serialPort = null;
        }
        else {
            LOGGER.info("Port is not opened");
        }

        connected = false;
    }

    public void write(byte[] data) throws IOException {
        outStream.write(data);
    }

    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
//                outputBufferEmpty(event);
                break;

            case SerialPortEvent.DATA_AVAILABLE:
                dataAvailable(event);
                break;

            case SerialPortEvent.BI:
//                breakInterrupt(event);
                break;

            case SerialPortEvent.CD:
//                carrierDetect(event);
                break;

            case SerialPortEvent.CTS:
//                clearToSend(event);
                break;

            case SerialPortEvent.DSR:
//                dataSetReady(event);
                break;

            case SerialPortEvent.FE:
//                framingError(event);
                break;

            case SerialPortEvent.OE:
//                overrunError(event);
                break;

            case SerialPortEvent.PE:
//                parityError(event);
                break;
            case SerialPortEvent.RI:
//                ringIndicator(event);
                break;
        }
    }

    private void dataAvailable(SerialPortEvent event) {
        System.out.println(event.getNewValue());
    }


    private boolean checkSerialPortParams() {
        return baudRate != -1 && dataBits != -1 && stopBits != -1 && parity != -1;
    }


    public static void main(String[] args) throws NoSuchPortException, IOException, InterruptedException {
        PhysicalLayer layer = new PhysicalLayer();
        for (String port : PhysicalLayer.getAvailablePorts()) {
            System.out.println(port);
        }

//        String port = "/dev/ttyS1600";
//        layer.setSerialPortParams(57600, 8, 1, 0);
//        layer.connect(port);
//
//        while (layer.isConnected()) {
//            String data = "Hello, world\n";
//            layer.write(data.getBytes());
//            Thread.sleep(500);
//        }
//
//        layer.disconnect();
    }


}
