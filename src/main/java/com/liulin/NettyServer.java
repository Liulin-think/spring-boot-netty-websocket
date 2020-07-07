package com.liulin;

import com.liulin.handler.ProcesssWsFrameHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author liulin_think
 * @Package com.liulin
 * @date 2020/7/6
 */
@Configuration
public class NettyServer {


    private static final String WEBSOCKET_PATH = "/websocket";
    public final static ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

    @Resource
    private ProcesssWsFrameHandler processsWsFrameHandler;

    public void startServer() {
        System.out.println("服务端启动成功");
        //创建两个线程组，用于接收客户端的请求任务,创建两个线程组是因为netty采用的是反应器设计模式
        //反应器设计模式中bossGroup线程组用于接收
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //workerGroup线程组用于处理任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建netty的启动类
        ServerBootstrap bootstrap = new ServerBootstrap();
        //创建一个通道
        ChannelFuture f = null;
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 设置日志
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 接收缓存
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                    // 是否保持连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //设置处理请求的逻辑处理类
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            /*增加对http的支持*/
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            /*Netty提供，支持WebSocket应答数据压缩传输*/
                            pipeline.addLast(new WebSocketServerCompressionHandler());
                            /**
                             * Netty提供，对整个websocket的通信进行了初始化(发现http报文中有升级为websocket的请求)
                             *         ，包括握手，以及以后的一些通信控制
                             */
                            pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
                            /*对websocket的数据进行处理*/
                            pipeline.addLast(processsWsFrameHandler);
                        }
                    });

            final Channel channel = bootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅退出
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


}
