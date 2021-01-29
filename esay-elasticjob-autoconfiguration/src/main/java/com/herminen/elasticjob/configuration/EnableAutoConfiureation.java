package com.herminen.elasticjob.configuration;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.herminen.elasticjob.annotation.parser.JobConfigureParser;
import com.herminen.elasticjob.property.ZookeeperProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * Created on 2021/1/28.
 *
 * @author herminen
 */
@EnableConfigurationProperties(value = {ZookeeperProperties.class})
@Configuration
@ConditionalOnProperty(name = {"serverLists", "namespace"}, prefix = "easy.elasticsearch.registration", matchIfMissing = false)
@Import(value = {JobConfigureParser.class})
public class EnableAutoConfiureation {

    @Resource
    private ZookeeperProperties zookeeperProperties;

    @Bean
    public ZookeeperConfiguration zookeeperConfiguration(){
        ZookeeperConfiguration zookeeperConfiguration =
                new ZookeeperConfiguration(zookeeperProperties.getServerLists(), zookeeperProperties.getNamespace());
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(zookeeperProperties.getBaseSleepTimeMilliseconds());
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(zookeeperProperties.getConnectionTimeoutMilliseconds());
        zookeeperConfiguration.setMaxRetries(zookeeperProperties.getMaxRetries());
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(zookeeperProperties.getMaxSleepTimeMilliseconds());
        zookeeperConfiguration.setSessionTimeoutMilliseconds(zookeeperProperties.getSessionTimeoutMilliseconds());
        zookeeperConfiguration.setDigest(zookeeperProperties.getDigest());
        return zookeeperConfiguration;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public ZookeeperRegistryCenter zookeeperRegistryCenter(){
        return new ZookeeperRegistryCenter(zookeeperConfiguration());
    }
}
