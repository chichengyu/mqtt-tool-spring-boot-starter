package com.mqtt.tool.annotation;

import com.mqtt.tool.core.IMqttTopicMessageHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主题类
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MqttTopic {

    /**
     * 主题名称
     * @return
     */
    String value() default "";

    /**
     * 是否 $queue/xx/xxx 消息
     * @return
     */
    boolean queue() default false;

    /**
     * 是否 $share/xx/xxx 消息
     * @return
     */
    boolean share() default false;

    /**
     * share为true时设置group
     * @return
     */
    String group() default "default";

}
