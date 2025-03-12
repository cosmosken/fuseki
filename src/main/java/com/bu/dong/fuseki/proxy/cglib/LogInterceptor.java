package com.bu.dong.fuseki.proxy.cglib;


import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class LogInterceptor implements MethodInterceptor {
    /**
     * @param obj         代理对象
     * @param method      目标方法
     * @param args        方法参数
     * @param methodProxy 方法代理（用于调用父类方法）
     */
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        System.out.println("[日志] 方法调用前: " + method.getName());
        // 调用父类（原始类）的方法
        Object result = methodProxy.invokeSuper(obj, args);
        System.out.println("[日志] 方法调用后: " + method.getName());
        return result;
    }
}

class UserService {
    public void addUser(String username) {
        System.out.println("添加用户: " + username);
    }

    public final void deleteUser(String username) {
        System.out.println("删除用户: " + username);
    }
}

class Main {
    public static void main(String[] args) {
        // 1. 创建 Enhancer 实例
        Enhancer enhancer = new Enhancer();
        // 2. 设置父类（目标类）
        enhancer.setSuperclass(UserService.class);
        // 3. 设置回调（方法拦截器）
        enhancer.setCallback(new LogInterceptor());
        // 4. 创建代理对象
        UserService proxy = (UserService) enhancer.create();
        // 5. 调用代理方法
        proxy.addUser("Alice");
        proxy.deleteUser("Bob");
    }
}