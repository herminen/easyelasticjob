package com.herminen.elasticjob.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on 2021/1/28.
 *
 * @author herminen
 */
@ConfigurationProperties(prefix = "easy.elasticsearch.registration")
@Data
public class ZookeeperProperties {

    /**
     * 连接Zookeeper服务器的列表
     * 包括IP地址和端口号
     * 多个地址用逗号分隔
     * 如: host1:2181,host2:2181
     */
    private String serverLists;
    /**
     * Zookeeper的命名空间
     */
    private String namespace;
    /**
     * 等待重试的间隔时间的初始值
     * 单位：毫秒
     */
    private Integer baseSleepTimeMilliseconds = 1000;
    /**
     * 等待重试的间隔时间的最大值
     * 单位：毫秒
     */
    private Integer maxSleepTimeMilliseconds = 3000;
    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;
    /**
     * 会话超时时间
     * 单位：毫秒
     */
    private Integer sessionTimeoutMilliseconds = 60000;
    /**
     * 连接超时时间
     * 单位：毫秒
     */
    private Integer connectionTimeoutMilliseconds = 15000;
    /**
     * 连接Zookeeper的权限令牌
     * 缺省为不需要权限验证
     */
    private String digest;

}
