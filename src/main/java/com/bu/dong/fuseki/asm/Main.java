package com.bu.dong.fuseki.asm;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Exception {
        // 生成增强后的字节码
        byte[] enhancedBytes = AsmEnhancer.enhanceProcessMethod();

        // 自定义类加载器加载增强类
        ClassLoader loader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if ("UserService".equals(name)) {
                    return defineClass(name, enhancedBytes, 0, enhancedBytes.length);
                }
                return super.findClass(name);
            }
        };

        // 反射调用增强后的方法
        Class<?> clazz = loader.loadClass("UserService");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod("process", String.class);
        method.invoke(instance, "user123");
    }
}