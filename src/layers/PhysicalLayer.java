package layers;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;

public class PhysicalLayer {

    public PhysicalLayer() {

    }

    public static void main(String[] args) {
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier port = (CommPortIdentifier) portEnum.nextElement();

            System.out.println(port.getName());
        }
    }
}
