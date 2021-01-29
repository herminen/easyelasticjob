package com.herminen.elasticjob.annotation;

import java.lang.annotation.*;

/**
 * Created on 2021/1/29.
 *
 * @author herminen
 */
@Repeatable (JobProperties.class)
public @interface JobProperty {
    String key();
    String value();
}
