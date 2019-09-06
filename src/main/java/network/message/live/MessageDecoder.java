package network.message.live;

import msgpack.MsgpackDecoder;

import java.io.IOException;

public class MessageDecoder extends MsgpackDecoder {

    private MessageCode code;

    public MessageDecoder(byte[] encode) throws IOException {
        super(encode);
        code = MessageCode.value(readInt());
    }

    public MessageCode getCode() {
        return code;
    }
}