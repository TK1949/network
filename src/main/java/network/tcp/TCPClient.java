package network.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import network.message.handshake.Ping;
import network.message.live.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TCPClient {

    private static final Logger logger = LoggerFactory.getLogger(TCPClient.class);

    private EventLoopGroup boos;
    private String ip;
    private int port;

    private Bootstrap boot;
    private int reconnection;

    private Channel channel;

    public TCPClient(EventLoopGroup boss, String ip, int port) {
        this.boos = boss;
        this.ip = ip;
        this.port = port;

        this.boot = new Bootstrap();
        this.reconnection = 0;
    }

    public void start() {
        try {
            channel = boot.group(boos)
                          .remoteAddress(ip, port)
                          .channel(NioSocketChannel.class)
                          .option(ChannelOption.TCP_NODELAY, true)
                          .handler(new ChannelInitializer<SocketChannel>() {
                              @Override
                              protected void initChannel(SocketChannel ch) {
                                  ch.pipeline().addLast(
                                          new IdleStateHandler(0, 0, 5000, TimeUnit.MILLISECONDS),
                                          new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8),
                                          new LengthFieldPrepender(8),
                                          new SocketFrameHandler());
                              }
                          }).connect().sync().channel();
        } catch (Exception e) {
            logger.error("Client -> start {}", e.getMessage());
            reconnection = 8;
        }
    }

    public void stop() {
        channel.close();
        boos.shutdownGracefully();
    }

    public void submission(byte[] msg) {
        if (reconnection == 0) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(msg));
        } else {
            logger.error("Client -> submission error");
        }
    }

    public boolean isActive() {
        return reconnection == 0;
    }

    private class SocketFrameHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            try {
                int length = msg.readableBytes();
                byte[] code = new byte[length];
                msg.getBytes(msg.readerIndex(), code, 0, length);
                msg.clear();

                MessageDecoder md = new MessageDecoder(code);

                switch (md.getCode()) {
                    case test:
                        System.out.println(new String(md.readByteArray()));
                        break;
                    default:
                        break;
                }

                System.out.println(ctx.channel().remoteAddress() + " : " + md.getCode());
            } catch (Exception e) {
                logger.error("Client -> channelRead0 {}", e.getMessage());
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {
            if (reconnection++ < 10) {
                ctx.channel().eventLoop().schedule(() -> {
                    try {
                        channel = boot.connect().addListener((ChannelFutureListener) future -> {
                            if (future.cause() == null) {
                                reconnection = 0;
                            }
                        }).sync().channel();
                    } catch (InterruptedException e) {
                        logger.error("Client -> channelUnregistered", e);
                    }
                }, 5000, TimeUnit.MILLISECONDS);
            } else {
                stop();
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                switch (e.state()) {
                    case ALL_IDLE:
                        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(Ping.single.encode()));
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
