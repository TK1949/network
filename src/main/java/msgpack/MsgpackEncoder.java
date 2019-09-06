package msgpack;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MsgpackEncoder {

    private ByteArrayOutputStream out;
    private Packer bp;

    public MsgpackEncoder() {
        this.out = new ByteArrayOutputStream();
        MessagePack mp = new MessagePack();
        this.bp  = mp.createPacker(out);
    }

    public MsgpackEncoder write(boolean o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(byte o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(short o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(int o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(long o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(float o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(double o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Boolean o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Byte o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Short o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Integer o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Long o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Float o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(Double o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(byte[] o) throws IOException {
        bp.write(o);
        return this;
    }

    public MsgpackEncoder write(String o) throws IOException {
        bp.write(o);
        return this;
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}