package network.web;

import io.netty.channel.nio.NioEventLoopGroup;

public class WebTest {

    public static void main(String[] args) throws InterruptedException {
        WebServer server = new WebServer(new NioEventLoopGroup(), new NioEventLoopGroup(), 8080);
        server.start();
    }
}
