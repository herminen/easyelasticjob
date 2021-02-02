package com.herminen.elasticjob.annotation.parser;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Preconditions;
import com.herminen.elasticjob.annotation.DataFlowJobConfiguration;
import com.herminen.elasticjob.annotation.JobProperty;
import javassist.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created with IntelliJ IDEA.
 * User: herminen
 * Date: 2021/1/28
 * Time: 22:15
 * Description: No Description
 */
@Slf4j
public class DataFlowJobConfigureParser extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        boolean annotationPresent = beanClass.isAnnotationPresent(DataFlowJobConfiguration.class);
        //检查是否需要转换成job调度
        if (!annotationPresent) {
            return super.postProcessBeforeInstantiation(beanClass, beanName);
        }
        DataFlowJobConfiguration annotation = beanClass.getDeclaredAnnotation(DataFlowJobConfiguration.class);
        Preconditions.checkNotNull(annotation.jobName());
        try {
            //利用javassist转换成SimpleJob类，并注入spring容器
            addJob(beanClass, annotation);
        } catch (NotFoundException |CannotCompileException |IllegalAccessException |InstantiationException e) {
            log.warn("add simple job  error}", e);
            return super.postProcessBeforeInstantiation(beanClass, beanName);
        }
        //构造和兴配置类
        JobCoreConfiguration jobCoreConfiguration = buildJobCoreConfiguration(annotation);
        JobTypeConfiguration jobTypeConfiguration;
        jobTypeConfiguration = buildDataflowJobConfiguration(beanClass, jobCoreConfiguration, annotation);
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration)
                .jobShardingStrategyClass(annotation.jobShardingStrategyClass()).overwrite(false).build();
        //启动调度任务
        new JobScheduler(applicationContext.getBean(ZookeeperRegistryCenter.class),
                liteJobConfiguration, applicationContext.getBean(JobEventConfiguration.class)).init();
        return super.postProcessBeforeInstantiation(beanClass, beanName);
    }

    /**
     * 动态代理实际类中的方法
     * @param beanClass
     * @param annotation
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void addJob(Class<?> beanClass, DataFlowJobConfiguration annotation) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        ClassPool pool = new ClassPool(true);
        CtClass jobClass = pool.get(beanClass.getName());
        //利用继承代理该类
        CtClass delegateCtClass = pool.makeClass(beanClass.getName() + "$Delegate", jobClass);
        delegateCtClass.addInterface(pool.getCtClass("com.dangdang.ddframe.job.api.dataflow.DataflowJob"));
        CtMethod executeMehthod = CtNewMethod.make("public void execute(com.dangdang.ddframe.job.api.ShardingContext shardingContext){" +
                "super." + annotation.fetchMethod() + "($$);" +
                "}", delegateCtClass);
        delegateCtClass.addMethod(executeMehthod);
        Class delegateClass  = delegateCtClass.toClass();
        listableBeanFactory.registerSingleton(delegateCtClass.getName() + "$SimpleJob", delegateClass.newInstance());
    }


    private JobTypeConfiguration buildDataflowJobConfiguration(Class<?> beanClass,JobCoreConfiguration jobCoreConfiguration, DataFlowJobConfiguration annotation) {
        return new DataflowJobConfiguration(jobCoreConfiguration, beanClass.getName()+ "$Delegate", annotation.streamingProcess());
    }

    private JobCoreConfiguration buildJobCoreConfiguration(DataFlowJobConfiguration annotation) {
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
