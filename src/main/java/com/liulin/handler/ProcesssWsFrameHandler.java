package com.liulin.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liulin.Application;
import com.liulin.NettyServer;
import com.liulin.request.BaseRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author liulin_think
 * @Package com.liulin
 * @date 2020/7/6
 */
@Component
@ChannelHandler.Sharable
public class ProcesssWsFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> implements ApplicationContextAware {


    public static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ProcesssWsFrameHandler.context = context;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        //判断是否为文本帧，目前只处理文本帧
        if (webSocketFrame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) webSocketFrame).text();
            System.out.printf("【%s】 received 【%s】\n", channelHandlerContext.channel(), request);
            final BaseRequest baseRequest = JSON.parseObject(request, BaseRequest.class);
            final Method method = Application.map.get(baseRequest.getCode());
            if (null != method) {
                final int parameterCount = method.getParameterCount();
                final Object invoke;
                final Object bean = context.getBean(method.getDeclaringClass());
                if (0 == parameterCount) {
                    invoke = method.invoke(bean);
                } else {
                    invoke = method.invoke(bean, baseRequest.getData());
                }
                channelHandlerContext.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(invoke)));
            }
        } else {
            String message = "unsupported frame type: " + webSocketFrame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * 重写 userEventTriggered()方法以处理自定义事件
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,
                                   Object evt) throws Exception {
        /*检测事件，如果是握手成功事件，做点业务处理*/
        if (evt == WebSocketServerProtocolHandler
                .ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {

            //通知所有已经连接的 WebSocket 客户端新的客户端已经连接上了
            NettyServer.CHANNEL_GROUP.writeAndFlush(new TextWebSocketFrame(
                    "Client " + ctx.channel() + " joined"));

            // 以便它可以接收到所有的消息
            NettyServer.CHANNEL_GROUP.add(ctx.channel());
//            ctx.channel().attr(AttributeKey.valueOf("userId")).set(atomicInteger.incrementAndGet());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }

}
