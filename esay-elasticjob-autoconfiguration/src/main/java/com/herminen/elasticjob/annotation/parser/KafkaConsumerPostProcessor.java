package com.herminen.elasticjob.annotation.parser;

import com.google.common.base.Joiner;
import com.herminen.elasticjob.annotation.RealTimeData;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created on 2021/7/15.
 *
 * @author ${AUTHOR}
 */
@Log
//@Component
public class KafkaConsumerPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private DefaultListableBeanFactory listableBeanFactory;

    private static final ClassPool CLASS_POOL = new ClassPool(true);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        autowireRealTimeDataWorkingBean(bean,beanName);
        return bean;
    }

    /**
     * 寻找并装载kafkaListener
     * @param bean
     * @param beanName
     */
    private void autowireRealTimeDataWorkingBean(Object bean, String beanName) {
        Method realTimeDataWorkingMethod = findRealTimeDataWorkingMethod(bean);
        if(null == realTimeDataWorkingMethod){
            return;
        }
        RealTimeData annotation = realTimeDataWorkingMethod.getAnnotation(RealTimeData.class);
        createRealTimeDataWorkingBean(beanName, bean, realTimeDataWorkingMethod, annotation);
    }

    private void createRealTimeDataWorkingBean(String beanName, Object bean, Method realTimeDataWorkingMethod, RealTimeData annotation) {
        String singletonBeanName = StringUtils.capitalize(beanName) +"$"+ StringUtils.capitalize(annotation.topic()) +"$"+ StringUtils.capitalize(annotation.group());
        try {
            //某个超类（这边随便写个）
            CtClass parseBean = CLASS_POOL.get(String.class.getName());
            CtClass workingBean = CLASS_POOL.makeClass(singletonBeanName, parseBean);
            workingBean.addInterface(CLASS_POOL.getCtClass(BeanFactoryAware.class.getName()));

            workingBean.addField(CtField.make("private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(\""+workingBean.getName()+"\");",workingBean));
            workingBean.addField(CtField.make("private org.springframework.beans.factory.support.DefaultListableBeanFactory listableBeanFactory;",workingBean));

            CtClass proxyClass = CLASS_POOL.getCtClass(bean.getClass().getName());
            CtField proxyClassField = new CtField(proxyClass, "workTarget", workingBean);
            workingBean.addField(proxyClassField);
            FieldInfo proxyClassFieldInfo = proxyClassField.getFieldInfo();
            ConstPool proxyFieldPool = proxyClassFieldInfo.getConstPool();
            AnnotationsAttribute proxyFieldAnnotationsAttribute = new AnnotationsAttribute(proxyFieldPool, AnnotationsAttribute.visibleTag);
            Annotation resourceAnnotation = new Annotation("javax.annotation.Resource", proxyFieldPool);
            proxyFieldAnnotationsAttribute.addAnnotation(resourceAnnotation);
            proxyClassFieldInfo.addAttribute(proxyFieldAnnotationsAttribute);

            CtMethod proxyMethod = addKafkaListenerCoreMethod(realTimeDataWorkingMethod, annotation, workingBean);

            workingBean.addMethod(proxyMethod);
            workingBean.addMethod(CtMethod.make("public void setBeanFactory(org.springframework.beans.factory.BeanFactory beanFactory){" +
                    "this.listableBeanFactory = (org.springframework.beans.factory.support.DefaultListableBeanFactory) beanFactory;" +
                    "}",workingBean));
            Class workBeanClass = workingBean.toClass();
            RootBeanDefinition beanDefinition = new RootBeanDefinition(workBeanClass);
            listableBeanFactory.registerBeanDefinition(singletonBeanName, beanDefinition);
            listableBeanFactory.getBean(workBeanClass);
//            KafkaListenerAnnotationBeanPostProcessor kafkaListenerAnnotationBeanPostProcessor = listableBeanFactory.getBean(KafkaListenerAnnotationBeanPostProcessor.class);
//            listableBeanFactory.registerSingleton(singletonBeanName, kafkaListenerAnnotationBeanPostProcessor.postProcessAfterInitialization(workingBean.toClass().newInstance(),singletonBeanName));
        } catch (CannotCompileException | NotFoundException e) {
            log.warning("make work bean error:\n" + e);
        }
    }

    private CtMethod addKafkaListenerCoreMethod(Method realTimeDataWorkingMethod, RealTimeData annotation, CtClass workingBean) throws CannotCompileException {
        CtMethod proxyMethod = CtMethod.make("public void onMessage(org.apache.kafka.clients.consumer.ConsumerRecord consumerRecord, org.springframework.kafka.support.Acknowledgment ack)" +
                "{" +
                "        java.util.Collection transfers = " +
                "                  this.listableBeanFactory.getBeansOfType(com.hnzhgyl.bdmp.bigdata.common.component.kafka.msg.trasfer.IFieldValueTransfer.class).values();" +
                "        com.hnzhgyl.bdmp.bigdata.common.component.kafka.msg.MysqlMessageBean mysqlMessageBean = this.parse(consumerRecord, transfers, \""+ Joiner.on(",").join(annotation.subsctribeOps()) +"\");" +
                "        if(null != mysqlMessageBean){" +
                "               try{" +
                "                   workTarget." + realTimeDataWorkingMethod.getName() + "(mysqlMessageBean);" +
                "                   ack.acknowledge();" +
                "               }" +
                "               catch(Exception e){" +
                "                   logger.warn(\"invoke business method error.\", e);" +
                "               }" +
                "        }" +
                "}", workingBean);
        MethodInfo methodInfo = proxyMethod.getMethodInfo();
        ConstPool cPool = methodInfo.getConstPool();
        AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(cPool, AnnotationsAttribute.visibleTag);
        Annotation kafkaAnnotation = new Annotation("org.springframework.kafka.annotation.KafkaListener", cPool);
        ArrayMemberValue amv = new ArrayMemberValue(cPool);
        StringMemberValue[] elements = {new StringMemberValue(annotation.topic(), cPool)};
        amv.setValue(elements);
        kafkaAnnotation.addMemberValue("topics", amv);
        kafkaAnnotation.addMemberValue("groupId", new StringMemberValue(annotation.group(), cPool));
        annotationsAttribute.addAnnotation(kafkaAnnotation);
        proxyMethod.getMethodInfo().addAttribute(annotationsAttribute);
        return proxyMethod;
    }

    private Method findRealTimeDataWorkingMethod(Object bean) {
        Method[] declaredMethods = bean.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if(declaredMethod.isAnnotationPresent(RealTimeData.class)){
                return declaredMethod;
            }
        }
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
    }
}
