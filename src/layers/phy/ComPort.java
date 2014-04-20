package layers.phy;

import gnu.io.*;
import layers.ILayer;
import layers.dll.DataLinkLayer;
import layers.dll.IDataLinkLayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

//TODO: rename to PhysicalLayer [and remove IComPort interface]
public class ComPort implements IPhysicalLayer {
    private static Logger LOGGER = Logger.getLogger("PhysicalLayerLogger");
    private static List<String> availablePorts;
    private static final String PORT_NAME = "ChatPort";
    private static final int TIME_OUT = 2000;

    private IDataLinkLayer dataLinkLayer;

    SerialEventListener eventListener;
    private SerialPort serialPort;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean connected = false;


    public ComPort() {

    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean status) {
        this.connected = status;
        dataLinkLayer.notifyConnectionChanged(status);
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
        return asList(300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200);
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

    @Override
    public void connect(Map<String, String> settings) throws NoSuchPortException, UnsupportedCommOperationException {
        String port = settings.get("port");

        LOGGER.info("Connecting to port " + port);
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);

        try {
            serialPort = (SerialPort) portId.open(PORT_NAME, TIME_OUT);
            LOGGER.info("Port " + port + " opened successfully");

            //TODO: sorry for fuckup, String -> int conversions needed, e.g. None -> 0 for parity
            serialPort.setSerialPortParams(9600, 8, 1, 0);
            //serialPort.setSerialPortParams(settings.getBaudRate(), settings.getDataBits(), settings.getStopBits(), settings.getParity());
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            outStream = serialPort.getOutputStream();
            inStream = serialPort.getInputStream();

        } catch (PortInUseException e) {
            LOGGER.warning("Port " + port + " is already in use");
        } catch (UnsupportedCommOperationException e) {
            LOGGER.severe("Unsupported com port params");
            disconnect();
            throw e;
        } catch (IOException e) {
            LOGGER.severe("Error while opening streams for serial port");
            disconnect();
        }

        try {
            eventListener = new SerialEventListener(inStream, outStream);
            serialPort.addEventListener(eventListener);
            serialPort.notifyOnDataAvailable(true);
//            serialPort.notifyOnOutputEmpty(true);
//            serialPort.notifyOnBreakInterrupt(true);
//            serialPort.notifyOnCarrierDetect(true);
//            serialPort.notifyOnCTS(true);
//            serialPort.notifyOnDSR(true);
//            serialPort.notifyOnFramingError(true);
//            serialPort.notifyOnOverrunError(true);
//            serialPort.notifyOnParityError(true);
//            serialPort.notifyOnRingIndicator(true);
        } catch (TooManyListenersException e) {
            LOGGER.severe("Too many listeners");
            disconnect();
        }

        setConnected(true);
        serialPort.setDTR(true);
        serialPort.setRTS(true);
    }

    @Override
    public synchronized void disconnect() {
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

        setConnected(false);
    }

    @Override
    public void write(byte[] data) throws IOException {
        serialPort.setRTS(true);
        outStream.write(data);
    }

    public static void main(String[] args) throws Exception {
        //TODO: replace with ProtocolStack
        IDataLinkLayer dll = new DataLinkLayer();
        IPhysicalLayer layer = new ComPort();
        layer.setUpperLayer(dll);

        ComPort.getAvailablePorts().forEach(System.out::println);

        String port = "COM3";
//        String port = "/dev/ttyS1300";
        //TODO: sorry once again
        //Setting should be Map, so it can be passed through abstract interface's connect method
        Map<String, String> settings = new HashMap<>();
        settings.put("port", port);
        layer.connect(settings);
        //layer.connect(new ComPortSettings(port, 9600, 8, 1, 0));

//        while (layer.isConnected()) {
//            layer.write("Hello, com port".getBytes());
////            layer.read();
//            Thread.sleep(1000);
//
//        }

        Thread t = new Thread() {
            public void run() {
                //the following line will keep this app alive for 1000 seconds,
                //waiting for events to occur and responding to them (printing incoming messages to console).
                try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
            }
        };
        t.start();

//        layer.disconnect();
    }

    @Override
    public IDataLinkLayer getUpperLayer() {
        return dataLinkLayer;
    }

    @Override
    public void setUpperLayer(ILayer layer) {
        dataLinkLayer = (IDataLinkLayer) layer;
    }
}
