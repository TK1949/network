package network.tcp;

import io.netty.channel.nio.NioEventLoopGroup;
import network.message.live.MessageCode;
import network.message.live.MessageEncoder;

import java.io.IOException;

public class TCPTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        TCPServer server = new TCPServer(new NioEventLoopGroup(), new NioEventLoopGroup(), 8080);
        server.start();

        TCPClient client = new TCPClient(new NioEventLoopGroup(), "127.0.0.1", 8080);
        client.start();

        // client 给 server 发信息
        MessageEncoder encoder = new MessageEncoder();
        encoder.write(MessageCode.test).write("吃了吗");
        client.submission(encoder.toByteArray());
        // server 给所有连接的 client 发信息
        MessageEncoder encoder2 = new MessageEncoder();
        encoder2.write(MessageCode.test).write("所有客户端大家好啊");
        server.push(encoder2.toByteArray());
    }
}