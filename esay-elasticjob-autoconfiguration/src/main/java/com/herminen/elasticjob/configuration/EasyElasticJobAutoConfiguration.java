package com.herminen.elasticjob.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.herminen.elasticjob.annotation.parser.JobConfigureParser;
import com.herminen.elasticjob.annotation.parser.SimpleJobConfigureParser;
import com.herminen.elasticjob.property.ZookeeperProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
@EnableConfigurationProperties(value = {ZookeeperProperties.class, DataSourceProperties.class})
@Configuration
@ConditionalOnProperty(name = {"server-lists", "namespace"}, prefix = "easy.elasticsearch.registration", matchIfMissing = false)
@Import(value = {JobConfigureParser.class, SimpleJobConfigureParser.class})
public class EasyElasticJobAutoConfiguration {

    @Resource
    private ZookeeperProperties zookeeperProperties;
    @Resource
    private DataSourceProperties dataSourceProperties;

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String userName;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
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
        ZookeeperRegistryCenter zookeeperRegistryCenter = new ZookeeperRegistryCenter(zookeeperConfiguration());
        zookeeperRegistryCenter.init();
        return zookeeperRegistryCenter;
    }

    @ConditionalOnProperty(name = {"url", "username", "password", "driver-class-name"}, prefix = "spring.datasource", matchIfMissing = false)
    @Bean
    public DataSource dataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dataSourceProperties.getUrl());
        druidDataSource.setUsername(dataSourceProperties.getUsername());
        druidDataSource.setPassword(dataSourceProperties.getPassword());
        druidDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        return druidDataSource;
    }

    @ConditionalOnBean({DataSource.class})
    @Bean
    public JobEventConfiguration jobEventConfiguration(){
        return new JobEventRdbConfiguration(dataSource());
    }
}
