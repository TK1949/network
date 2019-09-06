package network.web;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http/ws
 */
public class WebServer {

    private static final String ping_code = "ping";
    private static final String ws_path = "/websocket";

    private EventLoopGroup boos;
    private EventLoopGroup worker;
    private int port;

    public WebServer(EventLoopGroup boss, EventLoopGroup worker, int port) {
        this.boos = boss;
        this.worker = worker;
        this.port = port;
    }

    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(boos, worker)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                 ch.pipeline().addLast(new ReadTimeoutHandler(60))
                              .addLast(new WriteTimeoutHandler(60))
                              .addLast(new HttpServerCodec())
                              .addLast(new HttpObjectAggregator(65536))
                              .addLast(new WebFrameHandler());
             }
        });

        b.bind(port).sync().channel();
    }

    private class WebFrameHandler extends SimpleChannelInboundHandler<Object> {

        private WebSocketServerHandshaker handshaker;

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof FullHttpRequest) {
                httpFrame(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                webSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        private void httpFrame(ChannelHandlerContext ctx, FullHttpRequest msg) {
            if (! msg.decoderResult().isSuccess()) {
                httpFrame(ctx, msg, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }

            String uri = msg.uri();

            if (ws_path.equals(uri)) {
                webSocketLocation(ctx, msg);
                return;
            }

            System.out.println(ctx.channel().remoteAddress() + " < " + uri + " >");

            DefaultFullHttpResponse answer = new DefaultFullHttpResponse(HTTP_1_1, OK);
            answer.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ByteBuf buffer = Unpooled.copiedBuffer("server ok", CharsetUtil.UTF_8);
            answer.content().writeBytes(buffer);
            buffer.release();
            httpFrame(ctx, msg, answer);
        }

        private void httpFrame(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
            // 支持跨域
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
                         .set(ACCESS_CONTROL_ALLOW_ORIGIN,  "*")
                         .set(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept")
                         .set(ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE");

            if (res.status().code() != 200) {
                ByteBuf buf = Unpooled.wrappedBuffer(res.status().toString().getBytes());
                res.content().writeBytes(buf);
                buf.release();
                HttpUtil.setContentLength(res, res.content().readableBytes());
            }

            if (! HttpUtil.isKeepAlive(req) || !HttpUtil.isKeepAlive(res) || res.status().code() != 200) {
                ChannelFuture f = ctx.channel().writeAndFlush(res);
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }


        private void webSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
            if (msg instanceof TextWebSocketFrame)
            {
                String content = ((TextWebSocketFrame) msg).text();
                if (ping_code.equals(content)) {
                    return;
                }

                System.out.println(ctx.channel().remoteAddress() + " : " + content);

                byte[] encode = "server answer".getBytes();
                ctx.writeAndFlush(msg.retain().replace(Unpooled.wrappedBuffer(encode)));
            }
            else if (msg instanceof CloseWebSocketFrame)
            {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
            }
            else if (msg instanceof PingWebSocketFrame)
            {
                ctx.writeAndFlush(new PongWebSocketFrame(msg.content().retain()));
            }
            else if (msg instanceof BinaryWebSocketFrame)
            {
                ctx.writeAndFlush(msg.retain());
            }
        }

        private void webSocketLocation(ChannelHandlerContext ctx, FullHttpRequest msg) {
            WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory
                    (msg.headers().get(HttpHeaderNames.HOST) + msg.uri(), null, true, 1024 * 1024 * 2);
            handshaker = factory.newHandshaker(msg);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), msg);
            }
        }
    }
}