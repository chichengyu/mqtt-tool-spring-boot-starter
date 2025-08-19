package com.mqtt.tool.core;

public interface IMqttTemplate {

    /**
     * 发送消息
     * @param topic
     * @param data
     * @return
     */
    boolean publish(String topic, Object data);

    /**
     * 发送延迟消息
     * @param topic
     * @param data
     * @param seconds
     * @return
     */
    boolean delayPublish(String topic, Object data,int seconds);

    /**
     * 订阅主题
     * @param topic
     */
    boolean subscribe(String topic);

    /**
     * 取消订阅
     * @param topic
     */
    boolean unSubscribe(String topic);
}
