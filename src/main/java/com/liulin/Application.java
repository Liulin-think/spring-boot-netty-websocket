package com.liulin;

import com.liulin.annotation.BindCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    NettyServer nettyServer;

    private static String basePackagePath = "com.liulin.controller";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public static HashMap<Integer, Method> map = new HashMap<>();

    private static void initMethod() {
        try {
            ClassUtil.getClassForPackagePath(basePackagePath).stream().forEach(item -> {
                final Set<Method> method = ClassUtil.getMethod(item, BindCommand.class, true);
                bindCommand(method);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void bindCommand(Set<Method> methods) {
        for (Method method : methods) {
            final BindCommand annotation = method.getAnnotation(BindCommand.class);
            if (null != annotation) {
                map.put(annotation.code(), method);
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        /**
         * TODO
         * 1.序列化
         * 3.参数参数
         */
        initMethod();
        nettyServer.startServer();
    }
}
