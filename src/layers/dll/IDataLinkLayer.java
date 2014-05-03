package layers.dll;

import layers.ILayer;

public interface IDataLinkLayer extends ILayer {
    void send(byte[] msg);
    void receive(byte[] data);

    void handshakeFinished();
}
