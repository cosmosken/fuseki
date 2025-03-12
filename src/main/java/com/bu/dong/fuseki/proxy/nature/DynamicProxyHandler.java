package com.bu.dong.fuseki.proxy.nature;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface Service {
    void execute();
}

class RealService implements Service {
    public void execute() {
        System.out.println("Executing real service");
    }
}

public class DynamicProxyHandler implements InvocationHandler {
    private final Object target;

    public DynamicProxyHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        System.out.println("Before executing service");
        Object result = method.invoke(target, args);
        System.out.println("After executing service");
        return result;
    }
}

class Main {
    public static void main(String[] args) {
        Service realService = new RealService();
        Service proxy = (Service) Proxy.newProxyInstance(
                realService.getClass().getClassLoader(),
                realService.getClass().getInterfaces(),
                new DynamicProxyHandler(realService)
        );
        proxy.execute();
    }
}