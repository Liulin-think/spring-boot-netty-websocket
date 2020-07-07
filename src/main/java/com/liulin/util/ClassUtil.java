package com.liulin.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author liulin_think
 * @Package com.liulin
 * @date 2020/7/6
 */
public class ClassUtil {
    /**
     * 根据指定类获取含有指定注解的方法
     *
     * @param aClass     指定类
     * @param annotation 方法上的注解
     * @param needParent 是否搜索父类
     * @return
     */
    public static Set<Method> getMethod(Class aClass, final Class annotation, boolean needParent) {
        final Set<Method> collect = new HashSet<>();
        if (needParent) {
            if (null != aClass.getSuperclass()) {
                collect.addAll(getMethod(aClass.getSuperclass(), annotation, true));
            }
        }
        collect.addAll(Arrays.asList(aClass.getMethods()).stream().filter(method -> Optional.ofNullable(method.getAnnotation(annotation)).isPresent()).collect(Collectors.toSet()));
        return collect;
    }


    public static Set<Class> getClassForPackagePath(String packageClassName) throws IOException {
        Set<Class> result = new HashSet<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        // 加载系统所有类资源
        Resource[] resources = resourcePatternResolver.getResources("classpath*:" + packageClassName.replaceAll("[.]", "/") + "/**/*.class");
        // 把每一个class文件找出来
        for (Resource r : resources) {
            try {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
                Class<?> clazz = ClassUtils.forName(metadataReader.getClassMetadata().getClassName(), null);
                result.add(clazz);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (LinkageError linkageError) {
                linkageError.printStackTrace();
            } finally {
            }
        }
        return result;
    }


    public static void main(String[] args) throws ClassNotFoundException, IOException {
        getClassForPackagePath("com.sun").stream().forEach(item -> System.out.println(item.getName()));
//        final String name = BusinessController.class.getPackage().getName();
//        final Set<Class> classForPackageClassName = getClassForPackagePath(name);
//        classForPackageClassName
//                .stream().
//                forEach(
//                        clazz -> getMethod(clazz, BindCommand.class, true)
//                                .stream()
//                                .forEach(item -> System.out.println(item.getName()))
//                );

    }
}
