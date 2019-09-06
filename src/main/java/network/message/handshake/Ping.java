package network.message.handshake;

import network.message.live.MessageCode;
import network.message.live.MessageEncoder;
import network.message.live.MessageFace;

import java.io.IOException;

public class Ping implements MessageFace {

    public static final Ping single;

    static {
        single = new Ping();
    }

    private byte[] encode;

    private Ping() {
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
