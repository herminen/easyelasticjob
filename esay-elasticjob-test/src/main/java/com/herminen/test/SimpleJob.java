package com.herminen.test;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.herminen.elasticjob.annotation.SimpleJobConfiguration;

/**
 * Created on 2021/2/1.
 *
 * @author ${AUTHOR}
 */
@SimpleJobConfiguration(jobName = "testJob", corn = "0/5 * * * * ?", method = "execute", shardingItemParameters = "0=a,1=b", shardingTotalCount = 2)
public class SimpleJob {

    public void execute(ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        System.out.println("hello world! -- " + shardingItem);
    }
}
