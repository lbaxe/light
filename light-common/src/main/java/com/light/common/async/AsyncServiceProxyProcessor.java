package com.light.common.async;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.aop.interceptor.AsyncExecutionInterceptor;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.core.SmartClassLoader;

/**
 * 对指定Bean做异步代理
 */
public class AsyncServiceProxyProcessor extends ProxyProcessorSupport {
    public <T> T getProxy(T bean) {
        ProxyFactory proxyFactory = prepareProxyFactory(bean, bean.getClass().getName());
        if (!proxyFactory.isProxyTargetClass()) {
            evaluateProxyInterfaces(bean.getClass(), proxyFactory);
        }
        proxyFactory.addAdvisor(new AsyncServicePointcutAdvisor());
        // customizeProxyFactory(proxyFactory);

        // Use original ClassLoader if bean class not locally loaded in overriding class loader
        ClassLoader classLoader = getProxyClassLoader();
        if (classLoader instanceof SmartClassLoader && classLoader != bean.getClass().getClassLoader()) {
            classLoader = ((SmartClassLoader)classLoader).getOriginalClassLoader();
        }
        return (T)proxyFactory.getProxy(classLoader);
    }

    protected ProxyFactory prepareProxyFactory(Object bean, String beanName) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.copyFrom(this);
        proxyFactory.setTarget(bean);
        return proxyFactory;
    }

    /**
     * 切点顾问
     */
    public class AsyncServicePointcutAdvisor extends AbstractPointcutAdvisor {
        // 通知-拦截器
        private Advice advice;
        // 切点-拦截规则
        private Pointcut pointcut;

        public AsyncServicePointcutAdvisor() {
            advice = new AsyncExecutionInterceptor(null);
            pointcut = new AsyncServicePointcut();
        }

        @Override
        public Pointcut getPointcut() {
            return this.pointcut;
        }

        @Override
        public Advice getAdvice() {
            return this.advice;
        }
    }

    /**
     * AOP 切点方法，识别哪些类和方法需要增强
     */
    public class AsyncServicePointcut implements Pointcut {
        private final ClassFilter classFilter;

        private final MethodMatcher methodMatcher;

        public AsyncServicePointcut() {
            this.classFilter = new BeanClassFilter();
            this.methodMatcher = MethodMatcher.TRUE;
        }

        @Override
        public ClassFilter getClassFilter() {
            return this.classFilter;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return this.methodMatcher;
        }
    }

    public class BeanClassFilter implements ClassFilter {
        @Override
        public boolean matches(Class<?> clazz) {
            return true;
        }
    }

}
