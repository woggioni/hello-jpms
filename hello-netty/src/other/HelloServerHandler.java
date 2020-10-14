package net.woggioni.hello.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HelloServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        char[] msg = "hello, netty noon it".toCharArray();
        final ByteBuf buf = ctx.alloc().buffer(msg.length);
        for (char c : msg) {
            buf.writeChar(c);
        }

        final ChannelFuture f = ctx.writeAndFlush(buf);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                assert f == future;
                ctx.close();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // print exception information and close the connection
        cause.printStackTrace();
        ctx.close();
    }
}
