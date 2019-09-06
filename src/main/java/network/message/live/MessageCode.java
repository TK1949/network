package network.message.live;

public enum MessageCode {

    /**
     *
     */
    nan(-1),

    ping(0x0),
    pong(0x1);

    public final int code;

    MessageCode(int code) {
        this.code = code;
    }

    public static MessageCode value(int code) {
        for (MessageCode mc : values()) {
            if (mc.code == code) {
                return mc;
            }
        }
        return nan;
    }
}
