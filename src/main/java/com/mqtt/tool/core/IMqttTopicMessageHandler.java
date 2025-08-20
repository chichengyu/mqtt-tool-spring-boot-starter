package com.mqtt.tool.core;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 消息处理器
 * @param <T>
 */
public interface IMqttTopicMessageHandler<T> {

    /**
     * 消息处理(接收到消息)
     * @param topic
     * @param body
     * @throws Exception
     */
    void messageArrived(String topic, T body, MqttMessage message) throws Exception;

    /**
     * 对方收到消息(当消息的传递完成并收到所有确认时调用该方法)
     * @param topic
     * @param body
     */
    void deliveryComplete(String topic, T body, IMqttDeliveryToken iMqttDeliveryToken);
}
