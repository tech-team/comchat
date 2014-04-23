package layers;

import java.io.*;

public abstract class PDU implements Serializable {
    public byte[] serialize() throws SerializationException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
            return out.toByteArray();
        }
        catch (IOException e) {
            throw new SerializationException(e);
        }
    }
    public static PDU deserialize(byte[] data) throws Exception {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (PDU) is.readObject();
        }
        catch(IOException | ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }
}
