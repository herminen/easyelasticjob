package com.herminen.elasticjob.scanner;

import com.herminen.elasticjob.annotation.JobConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

public class ScannerImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override  
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        //BeanDefinition扫描器  
        ClassPathBeanDefinitionScanner scanner = new ScannerClassPathBeanDefinitionScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(JobConfiguration.class)); //基于注解类进行扫描过滤
        scanner.scan(ClassUtils.getPackageName(""));
    }  
}  