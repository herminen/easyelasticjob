package com.herminen.elasticjob.scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

/**
 * 工厂Bean：通过Spring代理工厂创建代理类 
 */
@Slf4j
public class ScannerFactoryBean implements FactoryBean<Object> {
    private ProxyFactory factory;
    private String interfaceClassName;  
  
    public void setInterfaceClassName(String interfaceClassName) {  
        this.interfaceClassName = interfaceClassName;  
          
        try {  
            factory.setInterfaces(Class.forName(interfaceClassName));  
        } catch (ClassNotFoundException e) {  
            log.warn(String.format("ScannerFactoryBean::setInterfaceClassName:class %s not found",interfaceClassName), e);
        }
    }  
  
    public ScannerFactoryBean(){  
        factory = new ProxyFactory(); //Spring代理工厂  
        factory.addAdvice(new CustomMethodInterceptor());  
    }  
      
    @Override  
    public Object getObject() throws Exception {  
        return factory.getProxy();  
    }  
      
    @Override  
    public Class<?> getObjectType() {  
        try {  
            if(StringUtils.hasText(interfaceClassName)){
                return Class.forName(interfaceClassName);  
            }  
        } catch (ClassNotFoundException e) {
            log.warn(String.format("ScannerFactoryBean::getObjectType:class %s not found",interfaceClassName), e);
        }  
        return null;  
    }  
      
    @Override  
    public boolean isSingleton() {  
        return true;  
    }  
}  