package com.herminen.elasticjob.annotation;

import com.herminen.elasticjob.configuration.EasyElasticJobAutoConfiureation;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created on 2021/2/1.
 *
 * @author ${AUTHOR}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EasyElasticJobAutoConfiureation.class)
public @interface EnableEasyElasticJobAutoConfiureation {
}
