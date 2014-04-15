package layers;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class ComPort {
    private static Logger LOGGER = Logger.getLogger("PhysicalLayerLogger");
    private static List<String> availablePorts;
    private static final String PORT_NAME = "ChatPort";
    private static final int TIME_OUT = 2000;

    SerialEventListener eventListener;
    private SerialPort serialPort;
    private OutputStream outStream;
    private InputStream inStream;
    private boolean connected = false;

    public ComPort() {

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

    public void connect(String port, int baudRate, int dataBits, int stopBits, int parity) throws NoSuchPortException, UnsupportedCommOperationException {
        LOGGER.info("Connecting to port " + port);
        CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);

        try {
            serialPort = (SerialPort) portId.open(PORT_NAME, TIME_OUT);
            LOGGER.info("Port " + port + " opened successfully");

            serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            outStream = serialPort.getOutputStream();
            inStream = serialPort.getInputStream();

            connected = true;

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
    }

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

        connected = false;
    }

    public void write(byte[] data) throws IOException {
        outStream.write(data);
    }

    public void read() {
        eventListener.dataAvailable(null);
    }

    private void dataAvailable(SerialPortEvent event) {
        System.out.println(event.getNewValue());
    }

    public static void main(String[] args) throws NoSuchPortException, IOException, InterruptedException, UnsupportedCommOperationException {
        ComPort layer = new ComPort();
        for (String port : ComPort.getAvailablePorts()) {
            System.out.println(port);
        }

        String port = "/dev/ttyS600";
//        String port = "/dev/ttyS1300";
        layer.connect(port, 9600, 8, 1, 0);

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
}
