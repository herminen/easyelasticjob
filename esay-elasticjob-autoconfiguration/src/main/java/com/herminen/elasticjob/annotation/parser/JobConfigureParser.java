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
import com.herminen.elasticjob.annotation.JobConfiguration;
import com.herminen.elasticjob.annotation.JobProperty;
import javassist.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashSet;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: herminen
 * Date: 2021/1/28
 * Time: 22:15
 * Description: No Description
 */
@Slf4j
public class JobConfigureParser extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        boolean annotationPresent = beanClass.isAnnotationPresent(JobConfiguration.class);
        if (!annotationPresent) {
            return super.postProcessBeforeInstantiation(beanClass, beanName);
        }
        HashSet<Class<?>> interfaces = Sets.newHashSet(beanClass.getInterfaces());
        if(!interfaces.contains(SimpleJob.class)){
            log.warn("{} is not a valid job configureation, please implements {com.dangdang.ddframe.job.api.simple.SimpleJob}", beanClass);
            return super.postProcessBeforeInstantiation(beanClass, beanName);
        }
        JobConfiguration annotation = beanClass.getDeclaredAnnotation(JobConfiguration.class);
        Preconditions.checkNotNull(annotation.jobName());
        Preconditions.checkNotNull(annotation.jobClass());
        JobCoreConfiguration jobCoreConfiguration = buildJobCoreConfiguration(annotation);
        JobTypeConfiguration jobTypeConfiguration;
        if (annotation.streamingProcess()) {
            jobTypeConfiguration = buildSimpleJobConfiguration(jobCoreConfiguration, annotation);
        } else{
            jobTypeConfiguration = buildDataflowJobConfiguration(jobCoreConfiguration, annotation);
        }
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration)
                .jobShardingStrategyClass(annotation.jobShardingStrategyClass()).overwrite(false).build();
        new JobScheduler(applicationContext.getBean(ZookeeperRegistryCenter.class),
                liteJobConfiguration, applicationContext.getBean(JobEventConfiguration.class)).init();
        return super.postProcessBeforeInstantiation(beanClass, beanName);
    }

    private JobTypeConfiguration buildDataflowJobConfiguration(JobCoreConfiguration jobCoreConfiguration, JobConfiguration annotation) {
        return new DataflowJobConfiguration(jobCoreConfiguration, annotation.jobClass(), annotation.streamingProcess());
    }

    private JobTypeConfiguration buildSimpleJobConfiguration(JobCoreConfiguration jobCoreConfiguration, JobConfiguration annotation) {
        return new SimpleJobConfiguration(jobCoreConfiguration, annotation.jobClass());
    }

    private JobCoreConfiguration buildJobCoreConfiguration(JobConfiguration annotation) {
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
