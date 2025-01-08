package com.light;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import sun.misc.ProxyGenerator;

@EnableAsync
@ServletComponentScan
@SpringBootApplication
public class LightApplication {
    public static void main(String[] args) {
        // SpringApplication.run(LightApplication.class, args);
        // 打印cglib生成的代理类class
        /*System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\cglibProxyClass");
        
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TestController.class);// 设置被代理类
        enhancer.setCallback(new MyMethodInterceptor());// 设置拦截器
        TestController proxyService = (TestController)enhancer.create();// 创建代理类*/
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

        Test1 test1 = (Test1)Proxy.newProxyInstance(Test1.class.getClassLoader(), new Class[] {Test1.class},
            new MyInvcationHandler(new TestController()));
        test1.test();
        generateClassFile(TestController.class, "Test1Proxy");
    }

    public static void generateClassFile(Class clazz, String proxyName) {

        // 根据类信息和提供的代理类名称，生成字节码
        byte[] classFile = ProxyGenerator.generateProxyClass(proxyName, clazz.getInterfaces());
        String paths = clazz.getResource(".").getPath();
        System.out.println(paths);
        FileOutputStream out = null;

        try {
            // 保留到硬盘中
            out = new FileOutputStream(paths + proxyName + ".class");
            out.write(classFile);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class MyInvcationHandler implements InvocationHandler {
        // 目标对象
        private Test1 target;

        public MyInvcationHandler(Test1 target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(target, args);
        }
    }
}
