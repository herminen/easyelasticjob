package com.herminen.elasticjob.scanner;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.util.StopWatch;

/**
 * 自定义拦截器 
 */  
public class CustomMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(org.aopalliance.intercept.MethodInvocation invocation) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object object = null;

        System.out.println(invocation.getMethod().getDeclaringClass().getName() + ": " + invocation.getMethod().getName());

        stopWatch.stop();
        System.out.println("执行时间:" + stopWatch.getTotalTimeMillis() + "ms");
        return object;
    }
}