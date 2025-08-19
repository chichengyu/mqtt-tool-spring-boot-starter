package com.mqtt.tool;

/**
 * 分布式消息分发数据交互上下文
 */
public class MqttConttext {

    private final static ThreadLocal<String> me = new InheritableThreadLocal<String>();

    public static void set(String data){
        me.set(data);
    }

    public static String get(){
        return me.get();
    }

    public static void remove(){
        me.remove();
    }
}
