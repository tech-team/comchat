package layers;

import layers.phy.SerialzationException;

import java.io.*;

public abstract class PDU implements Serializable {
    public byte[] serialize() throws SerialzationException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
            return out.toByteArray();
        }
        catch (IOException e) {
            throw new SerialzationException(e);
        }
    }
    public static PDU deserialize(byte[] data) throws SerialzationException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (PDU) is.readObject();
        }
        catch(IOException | ClassNotFoundException e) {
            throw new SerialzationException(e);
        }
    }
}
