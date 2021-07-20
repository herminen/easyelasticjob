package com.herminen.elasticjob.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.herminen.elasticjob.annotation.RealTimeData.OP_Enum.*;

/**
 * Created on 2021/7/15.
 *
 * @author ${AUTHOR}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RealTimeData {

    String topic();

    String group();

    OP_Enum[] subsctribeOps() default {INSERT, UPDATE, DELETE, SELECT};


    enum OP_Enum{
        ALL(""),
        INSERT("c"),
        UPDATE("u"),
        DELETE("d"),
        SELECT("r"),
        ;
        private String _op;

        OP_Enum(String _op) {
            this._op = _op;
        }

        @Override
        public String toString() {
            return _op;
        }
    }
}
