package com.herminen.elasticjob.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.herminen.elasticjob.annotation.parser.JobConfigureParser;
import com.herminen.elasticjob.property.ZookeeperProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Created on 2021/1/28.
 *
 * @author herminen
 */
@EnableConfigurationProperties(value = {ZookeeperProperties.class})
@Configuration
@ConditionalOnProperty(name = {"serverLists", "namespace"}, prefix = "easy.elasticsearch.registration", matchIfMissing = false)
@Import(value = {JobConfigureParser.class})
public class EasyElasticJobAutoConfiureation {

    @Resource
    private ZookeeperProperties zookeeperProperties;

    @Value("spring.datasource.url")
    private String url;
    @Value("spring.datasource.data-username")
    private String userName;
    @Value("spring.datasource.data-password")
    private String password;
    @Value("spring.datasource.driver-class-name")
    private String driverClass;

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

    @ConditionalOnProperty(name = {"url", "data-username", "data-password", "driver-class-name"}, prefix = "spring.datasource", matchIfMissing = false)
    @Bean
    public DataSource dataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(userName);
        druidDataSource.setUsername(userName);
        druidDataSource.setDriverClassName(driverClass);
        return new DruidDataSource();
    }

    @ConditionalOnBean(name = "dataSource")
    @Bean
    public JobEventConfiguration jobEventConfiguration(){
        return new JobEventRdbConfiguration(dataSource());
    }
}
