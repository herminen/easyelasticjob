package com.herminen.elasticjob.configuration;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.herminen.elasticjob.property.ZookeeperProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Created on 2021/1/28.
 *
 * @author ${AUTHOR}
 */
@EnableConfigurationProperties(value = {ZookeeperProperties.class})
@Configuration
@ConditionalOnProperty(name = {"serverLists", "namespace"}, prefix = "easy.elasticsearch.registration", matchIfMissing = false)
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
}
