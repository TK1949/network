package msgpack;

import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MsgpackDecoder {

    private byte[] encode;
    private Unpacker bu;

    public MsgpackDecoder(byte[] encode) {
        this.encode = encode;
        ByteArrayInputStream in = new ByteArrayInputStream(encode);
        MessagePack mp = new MessagePack();
        this.bu = mp.createUnpacker(in);
    }

    public boolean readBoolean() throws IOException {
        return bu.readBoolean();
    }

    public byte readByte() throws IOException {
        return bu.readByte();
    }

    public short readShort() throws IOException {
        return bu.readShort();
    }

    public int readInt() throws IOException {
        return bu.readInt();
    }

    public long readLong() throws IOException {
        return bu.readLong();
    }

    public float readFloat() throws IOException {
        return bu.readFloat();
    }

    public double readDouble() throws IOException {
        return bu.readDouble();
    }

    public byte[] readByteArray() throws IOException {
        return bu.readByteArray();
    }

    public String readString() throws IOException {
        return bu.readString();
    }

    public byte[] getEncode() {
        return encode;
    }
}
