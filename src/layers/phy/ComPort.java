package layers.phy;

import gnu.io.*;
import layers.ILayer;
import layers.ProtocolStack;
import layers.apl.ApplicationLayer;
import layers.dll.DataLinkLayer;
import layers.dll.IDataLinkLayer;
import layers.phy.settings.PhysicalLayerSettings;
import layers.phy.settings.comport_settings.ComPortSettings;
import layers.phy.settings.comport_settings.DataBitsEnum;
import layers.phy.settings.comport_settings.ParityEnum;
import layers.phy.settings.comport_settings.StopBitsEnum;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class ComPort implements IPhysicalLayer, SerialPortEventListener {
    private static Logger LOGGER = Logger.getLogger("PhysicalLayerLogger");
    private static List<String> availablePorts;
    private static final String PORT_NAME = "ChatPort";
    private static final int TIME_OUT = 2000;

    private IDataLinkLayer dataLinkLayer;

    private SerialPort serialPort;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean connected = false;

    private List<Consumer<Boolean>> connectionChangedListeners = new LinkedList<>();


    public ComPort() {

    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean status) {
        this.connected = status;
        notifyConnectionStatusChanged(status);
    }

    public static List<String> getAvailablePorts(boolean ignoreCache) {
        if (!ignoreCache && availablePorts != null)
            return availablePorts;

        availablePorts = new LinkedList<>();

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

    public static List<String> getAvailableDataBits() {
        List<String> names = new LinkedList<>();
        stream(DataBitsEnum.values()).forEach(e -> {
            names.add(e.toString());
        });
        return names;
    }

    public static List<String> getAvailableStopBits() {
        List<String> names = new LinkedList<>();
        stream(StopBitsEnum.values()).forEach(e -> {
            names.add(e.toString());
        });
        return names;
    }

    public static List<String> getAvailableParity() {
        List<String> names = new LinkedList<>();
        stream(ParityEnum.values()).forEach(e -> {
            names.add(e.toString());
        });
        return names;
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

    public static String getDefaultDataBits() {
        return DataBitsEnum.getDefault().toString();
    }

    public static String getDefaultStopBits() {
        return StopBitsEnum.getDefault().toString();
    }

    public static String getDefaultParity() {
        return ParityEnum.getDefault().toString();
    }

    @Override
    public void connect(PhysicalLayerSettings settings) throws NoSuchPortException, UnsupportedCommOperationException {
        connect((ComPortSettings) settings);
    }

    private void connect(ComPortSettings settings) throws NoSuchPortException, UnsupportedCommOperationException {
        String port = settings.getPort();

        LOGGER.info("Connecting to port " + port);
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);

        try {
            serialPort = (SerialPort) portId.open(PORT_NAME, TIME_OUT);
            LOGGER.info("Port " + port + " opened successfully");

            serialPort.setSerialPortParams(settings.getBaudRate(), settings.getDataBits(), settings.getStopBits(), settings.getParity());
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
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
//            serialPort.notifyOnOutputEmpty(true);
//            serialPort.notifyOnBreakInterrupt(true);
//            serialPort.notifyOnCarrierDetect(true);
            serialPort.notifyOnCTS(true);
            serialPort.notifyOnDSR(true);
//            serialPort.notifyOnFramingError(true);
//            serialPort.notifyOnOverrunError(true);
//            serialPort.notifyOnParityError(true);
//            serialPort.notifyOnRingIndicator(true);
        } catch (TooManyListenersException e) {
            LOGGER.severe("Too many listeners");
            disconnect();
        }

        serialPort.setRTS(true);
        serialPort.setDTR(true);
        setConnected(true);
    }

    @Override
    public synchronized void disconnect() {
        if (serialPort != null) {
            try {
                outStream.close();
                inStream.close();
            } catch (IOException ignored) {
            }

            serialPort.setRTS(false);
            serialPort.setDTR(false);

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
    public void send(byte[] data) throws IOException {
//        serialPort.setDTR(true);
        System.out.println("ready? - " + readyToSend());
        serialPort.setRTS(false);
        outStream.write(data);
        try {
            outStream.flush();
        } catch (IOException ignored) { }
        serialPort.setRTS(true);
    }

    @Override
    public boolean readyToSend() {
        return serialPort.isCTS() && serialPort.isDSR();
    }

    @Override
    public IDataLinkLayer getUpperLayer() {
        return dataLinkLayer;
    }

    @Override
    public ILayer getLowerLayer() {
        return null;
    }

    @Override
    public void setUpperLayer(ILayer layer) {
        dataLinkLayer = (IDataLinkLayer) layer;
    }

    @Override
    public void setLowerLayer(ILayer layer) {

    }

    @Override
    public void subscribeConnectionStatusChanged(Consumer<Boolean> listener) {
        connectionChangedListeners.add(listener);
    }

    private void notifyConnectionStatusChanged(boolean status) {
        connectionChangedListeners.forEach(listener -> listener.accept(status));
    }


    @Override
    public synchronized void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                System.out.println("OUTPUT_BUFFER_EMPTY");
//                outputBufferEmpty(event);
                break;

            case SerialPortEvent.DATA_AVAILABLE:
                System.out.println("DATA_AVAILABLE");
                dataAvailable(event);
                break;

            case SerialPortEvent.BI:
                System.out.println("BI");
//                breakInterrupt(event);
                break;

            case SerialPortEvent.CD:
                System.out.println("CD");
//                carrierDetect(event);
                break;

            case SerialPortEvent.CTS:
                System.out.println("CTS");
//                clearToSend(event);
                break;

            case SerialPortEvent.DSR:
                System.out.println("DSR");
//                dataSetReady(event);
                break;

            case SerialPortEvent.FE:
                System.out.println("FE");
//                framingError(event);
                break;

            case SerialPortEvent.OE:
                System.out.println("OE");
//                overrunError(event);
                break;

            case SerialPortEvent.PE:
                System.out.println("PE");
//                parityError(event);
                break;
            case SerialPortEvent.RI:
                System.out.println("RI");
//                ringIndicator(event);
                break;
        }
    }

    public void dataAvailable(SerialPortEvent event) {
        // reading size of the data
        try {
            byte[] dataSize = new byte[4];
            for (int i = 0; i < dataSize.length; ++i) {
                dataSize[i] = (byte) inStream.read();
            }

            // reading the whole data
            int size = ByteBuffer.wrap(dataSize).getInt();
            byte[] data = new byte[size];
            for (int i = 0; i < size; ++i) {
                data[i] = (byte) inStream.read();
            }

            getUpperLayer().receive(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: add own exceptions
//        Scanner scanner = new Scanner(inStream);
//        String line = scanner.nextLine();
//        System.out.println(line);
    }







    private static void reader(IPhysicalLayer comPort, String port) throws Exception {
        comPort.connect(new ComPortSettings(port, 9600, 8, 1, 0));
    }

    private static void writer(IPhysicalLayer comPort, String port) throws Exception {
        comPort.connect(new ComPortSettings(port, 9600, 8, 1, 0));

        while (comPort.isConnected()) {
            comPort.send("Привет!".getBytes());
//            layer.read();
            Thread.sleep(2000);

        }
    }


    public static void main(String[] args) throws Exception {
        ProtocolStack stack = new ProtocolStack(ApplicationLayer.class, DataLinkLayer.class, ComPort.class);
        IPhysicalLayer comPort = stack.getPhy();

        ComPort.getAvailablePorts().forEach(System.out::println);

        for (String arg : args) {
            switch (arg) {
                case "reader":
                    reader(comPort, "COM5");
                    break;
                case "writer":
                    writer(comPort, "COM6");
                    break;
            }
        }

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
}
