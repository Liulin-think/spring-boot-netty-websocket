package com.liulin.controller;

import com.liulin.NettyServer;
import com.liulin.annotation.BindCommand;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

/**
 * @author liulin_think
 * @Package com.liulin.controller
 * @date 2020/7/6
 */
@Component
public class BusinessController {
    @BindCommand(code = 100)
    public String sayHi() {
        return "hi";
    }

    @BindCommand(code = 101)
    public String sayHi(String name) {
        NettyServer.CHANNEL_GROUP.stream().filter(item -> (int) (item.pipeline().channel().attr(AttributeKey.valueOf("userId")).get()) > 1).forEach(item -> item.writeAndFlush("指定用户发送数据"));
        return "hi:" + name;
    }

    @BindCommand(code = 102)
    public void sayNothing(String name) {
        System.out.println("hi:" + name);
    }

    @BindCommand(code = 103)
    public void say(String name) {
        System.out.println("hi:" + name);
    }
}
