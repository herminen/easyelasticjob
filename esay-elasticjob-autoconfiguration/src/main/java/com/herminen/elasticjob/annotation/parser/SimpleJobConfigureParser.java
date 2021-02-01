package com.herminen.elasticjob.annotation.parser;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.herminen.elasticjob.annotation.JobProperty;
import javassist.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: herminen
 * Date: 2021/1/28
 * Time: 22:15
 * Description: No Description
 */
@Slf4j
public class SimpleJobConfigureParser extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        boolean annotationPresent = beanClass.isAnnotationPresent(com.herminen.elasticjob.annotation.SimpleJobConfiguration.class);
        if (!annotationPresent) {
            return super.postProcessBeforeInstantiation(beanClass, beanName);
        }
        com.herminen.elasticjob.annotation.SimpleJobConfiguration annotation = beanClass.getDeclaredAnnotation(com.herminen.elasticjob.annotation.SimpleJobConfiguration.class);
        Preconditions.checkNotNull(annotation.jobName());
        try {
            addJob(beanClass, annotation);
        } catch (NotFoundException |CannotCompileException |IllegalAccessException |InstantiationException e) {
            log.warn("add simple job  error}", e);
        }
        JobCoreConfiguration jobCoreConfiguration = buildJobCoreConfiguration(annotation);
        JobTypeConfiguration jobTypeConfiguration;
        if (annotation.streamingProcess()) {
            jobTypeConfiguration = buildSimpleJobConfiguration(beanClass, jobCoreConfiguration, annotation);
        } else{
            jobTypeConfiguration = buildDataflowJobConfiguration(beanClass, jobCoreConfiguration, annotation);
        }
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration)
                .jobShardingStrategyClass(annotation.jobShardingStrategyClass()).overwrite(false).build();
        new JobScheduler(applicationContext.getBean(ZookeeperRegistryCenter.class),
                liteJobConfiguration, applicationContext.getBean(JobEventConfiguration.class)).init();
        return super.postProcessBeforeInstantiation(beanClass, beanName);
    }

    private void addJob(Class<?> beanClass, com.herminen.elasticjob.annotation.SimpleJobConfiguration annotation) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        ClassPool pool = new ClassPool(true);
        CtClass jobClass = pool.get(beanClass.getName());
        CtClass delegateCtClass = pool.makeClass(beanClass.getName() + "$Delegate", jobClass);
        delegateCtClass.addInterface(pool.getCtClass("com.dangdang.ddframe.job.api.simple.SimpleJob"));
        CtMethod executeMehthod = CtNewMethod.make("public void execute(com.dangdang.ddframe.job.api.ShardingContext shardingContext){" +
                "super." + annotation.method() + "($$);" +
                "}", delegateCtClass);
        delegateCtClass.addMethod(executeMehthod);
        Class delegateClass  = delegateCtClass.toClass();
        listableBeanFactory.registerSingleton(delegateCtClass.getName() + "$SimpleJob", delegateClass.newInstance());
    }


    private JobTypeConfiguration buildDataflowJobConfiguration(Class<?> beanClass,JobCoreConfiguration jobCoreConfiguration, com.herminen.elasticjob.annotation.SimpleJobConfiguration annotation) {
        return new SimpleJobConfiguration(jobCoreConfiguration, beanClass.getName()+ "$Delegate");
    }

    private JobTypeConfiguration buildSimpleJobConfiguration(Class<?> beanClass, JobCoreConfiguration jobCoreConfiguration, com.herminen.elasticjob.annotation.SimpleJobConfiguration annotation) {
        return new DataflowJobConfiguration(jobCoreConfiguration, beanClass.getName()+ "$Delegate", annotation.streamingProcess());
    }

    private JobCoreConfiguration buildJobCoreConfiguration(com.herminen.elasticjob.annotation.SimpleJobConfiguration annotation) {
        JobCoreConfiguration.Builder jobCoreConfigurationbuilder = JobCoreConfiguration
                        .newBuilder(annotation.jobName(), annotation.corn(), annotation.shardingTotalCount())
                        .jobParameter(annotation.jobParameter()).description(annotation.description())
                        .failover(annotation.failover()).misfire(annotation.failover())
                        .shardingItemParameters(annotation.shardingItemParameters());
        JobProperty[] jobProperties = annotation.jobProperty();
        for (JobProperty jobProperty : jobProperties) {
            jobCoreConfigurationbuilder.jobProperties(jobProperty.key(), jobProperty.value());
        }
        return jobCoreConfigurationbuilder.build();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
