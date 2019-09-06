package network.tcp;

import io.netty.channel.nio.NioEventLoopGroup;

public class TCPTest {

    public static void main(String[] args) throws InterruptedException {
        TCPServer server = new TCPServer(new NioEventLoopGroup(), new NioEventLoopGroup(), 8080);
        server.start();

        TCPClient client = new TCPClient(new NioEventLoopGroup(), "127.0.0.1", 8080);
        client.start();
    }
}