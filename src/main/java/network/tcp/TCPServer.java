package network.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import network.message.live.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServer {

    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    private EventLoopGroup boos;
    private EventLoopGroup worker;
    private int port;

    private ChannelGroup channels;

    public TCPServer(EventLoopGroup boss, EventLoopGroup worker, int port) {
        this.boos = boss;
        this.worker = worker;
        this.port = port;
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(boos, worker)
         .channel(NioServerSocketChannel.class)
         .option(ChannelOption.SO_BACKLOG, 1024 * 2)
         .childHandler(new ChannelInitializer<SocketChannel>() {
             @Override
             protected void initChannel(SocketChannel ch) {
                 ch.pipeline().addLast(new ReadTimeoutHandler(60))
                              .addLast(new WriteTimeoutHandler(60))
                              .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8))
                              .addLast(new LengthFieldPrepender(8))
                              .addLast(new SocketFrameHandler());
             }
         }).bind(port).sync().channel();
    }

    public void stop() {
        boos.shutdownGracefully();
        worker.shutdownGracefully();
    }

    public void push(byte[] msg) {
        channels.writeAndFlush(Unpooled.wrappedBuffer(msg));
    }

    private class SocketFrameHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            channels.add(ctx.channel());
            super.channelRegistered(ctx);
        }

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
                logger.error("Server -> channelRead0 {}", e.getMessage());
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            channels.remove(ctx.channel());
            super.channelUnregistered(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}