package com.herminen.elasticjob.scanner;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * 自定义BeanDefinition扫描器 
 */  
public class ScannerClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
    private ScannerFactoryBean scannerFactoryBean = new ScannerFactoryBean();
      
    public ScannerClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);  
    }  
      
    @Override  
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);  
          
        ScannedGenericBeanDefinition beanDefinition;
        for(BeanDefinitionHolder holder : beanDefinitions){  
            beanDefinition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();  
            if(beanDefinition.getMetadata().isInterface()){ //是接口类  
                //添加BeanClass的属性值  
                beanDefinition.getPropertyValues().add("interfaceClassName", beanDefinition.getBeanClassName());  
  
                beanDefinition.setBeanClass(scannerFactoryBean.getClass()); //通过工厂Bean获取Bean对象  
            }  
        }  
          
        return beanDefinitions;  
    }  
      
    /** 
     * 是候选组件 
     */  
    @Override  
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
          
        //isIndependent独立的、isConcrete具体的、isInterface接口  
        return metadata.isIndependent() && (metadata.isConcrete() || metadata.isInterface());  
    }  
}  