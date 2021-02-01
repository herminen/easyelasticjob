package com.herminen;

import com.herminen.elasticjob.annotation.EnableEasyElasticJobAutoConfiureation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created on 2021/2/1.
 *
 * @author ${AUTHOR}
 */
@EnableEasyElasticJobAutoConfiureation
@SpringBootApplication
public class ElasJobTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasJobTestApplication.class, args);
    }
}
