package com.herminen.elasticjob.annotation.parser;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created with IntelliJ IDEA.
 * User: herminen
 * Date: 2021/1/28
 * Time: 22:15
 * Description: No Description
 */
public class JobConfigureParser implements ApplicationContextAware, InitializingBean {

    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
