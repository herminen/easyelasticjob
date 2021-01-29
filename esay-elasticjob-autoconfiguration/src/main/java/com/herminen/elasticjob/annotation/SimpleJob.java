package com.herminen.elasticjob.annotation;

import java.lang.annotation.*;

/**
 * Created on 2021/1/29.
 *
 * @author herminen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleJob {

}
