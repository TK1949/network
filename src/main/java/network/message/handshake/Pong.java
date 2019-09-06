package network.message.handshake;

import network.message.live.MessageCode;
import network.message.live.MessageEncoder;
import network.message.live.MessageFace;

import java.io.IOException;

public class Pong implements MessageFace {

    public static final Pong single;

    static {
        single = new Pong();
    }

    private byte[] encode;

    private Pong() {
        try {
            this.encode = new MessageEncoder().write(MessageCode.ping).toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] encode() {
        return encode;
    }

}
