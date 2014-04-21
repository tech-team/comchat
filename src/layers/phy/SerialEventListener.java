package layers.phy;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class SerialEventListener implements SerialPortEventListener {
    private InputStream inStream;
    private OutputStream outStream;

    public SerialEventListener(InputStream inStream, OutputStream outStream) {
        this.inStream = inStream;
        this.outStream = outStream;
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
        Scanner scanner = new Scanner(inStream);
        String line = scanner.nextLine();
        System.out.println(line);
    }
}
