package network.message.live;

import msgpack.MsgpackEncoder;

import java.io.IOException;

public class MessageEncoder extends MsgpackEncoder {

    public MessageEncoder write(MessageCode o) throws IOException {
        write(o.code);
        return this;
    }
}