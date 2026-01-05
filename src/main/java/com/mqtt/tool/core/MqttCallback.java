package com.mqtt.tool.core;


import com.alibaba.fastjson2.JSON;
import com.mqtt.tool.MqttConttext;
import com.mqtt.tool.MqttTemplate;
import com.mqtt.tool.annotation.MqttTopic;
import com.mqtt.tool.body.MessageBody;
import com.mqtt.tool.config.ContextConfig;
import com.mqtt.tool.util.MqttTopicMatcher;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class MqttCallback implements MqttCallbackExtended {
    private static Logger LOGGER = LoggerFactory.getLogger(MqttCallback.class);

    private static ConcurrentHashMap<String,IMqttTopicMessageHandler<Object>> handlerContainer = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String,IMqttTopicMessageHandler<Object>> getHandlerContainer(){
        return MqttCallback.handlerContainer;
    }

    /**
     * MQTT连接成功
     * @param reconnect
     * @param serverURI
     */
    @Override
    @SuppressWarnings("unchecked")
    public void connectComplete(boolean reconnect, String serverURI) {
        LOGGER.info("MQTT connection successful");
        StringBuilder msgTopic = new StringBuilder();
        if (ContextConfig.applicationContext != null){
            // 通过接口获取所有实现类
            String[] beanNamesForType = ContextConfig.applicationContext.getBeanNamesForType(IMqttTopicMessageHandler.class);
            // 通过注解获取所有被注解标识的类
            //String[] beanNamesForAnnotation = ContextConfig.applicationContext.getBeanNamesForAnnotation(MqttTopic.class);
            for (String beanName : beanNamesForType) {
                //MqttTemplate.getMqttTemplate().subscribe();
                Class<?> beanType = ContextConfig.applicationContext.getType(beanName);
                if (beanType != null && beanType.isAnnotationPresent(MqttTopic.class)){
                    MqttTopic mqttTopic = beanType.getAnnotation(MqttTopic.class);
                    if (!"".equals(mqttTopic.value())){
                        MqttTemplate.getMqttTemplate().subscribe(mqttTopic.value(),mqttTopic.queue(),mqttTopic.share(),mqttTopic.group());
                        IMqttTopicMessageHandler<Object> targetClass = (IMqttTopicMessageHandler<Object>) ContextConfig.applicationContext.getBean(beanName);
                        handlerContainer.put(mqttTopic.value(),targetClass);
                        msgTopic.append(beanName).append(".");
                    }
                }
            }
        }
        LOGGER.info("register MQTT topic IMqttTopicMessage:{}",msgTopic.toString());
    }

    /**
     * 连接断开
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        LOGGER.info("MQTT连接丢失.",throwable);
    }

    /**
     * 接收消息
     * @param topic
     * @param message
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());//获取emq发过来的消息体
        LOGGER.info("messageArrived==>Topic:{},Id:{},Qos:{},Retained:{},Message:{}",topic,message.getId(),message.getQos(),message.isRetained(),payload);
        if (handlerContainer.containsKey(topic)){
            if (MqttTemplate.getMqttConfig().getExecutor() != null){
                CompletableFuture.runAsync(() -> sendMessageArrived(handlerContainer.get(topic),topic, message, payload),MqttTemplate.getMqttConfig().getExecutor());
            }else {
                sendMessageArrived(handlerContainer.get(topic),topic, message, payload);
            }
        }else {
            Executor executor = MqttTemplate.getMqttConfig().getExecutor();
            Map<String, IMqttTopicMessageHandler<Object>> sendTopicMap = getSendTopic(topic);
            for (Map.Entry<String, IMqttTopicMessageHandler<Object>> entry : sendTopicMap.entrySet()) {
                if (executor != null){
                    CompletableFuture.runAsync(() -> sendMessageArrived(entry.getValue(), entry.getKey(), message, payload), executor);
                }else {
                    sendMessageArrived(entry.getValue(), entry.getKey(), message, payload);
                }
            }
        }
    }

    /**
     * 对方收到消息
     * @param iMqttDeliveryToken
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //当消息的传递完成并收到所有确认时调用该方法
        // 返回操作是否已完成。*<p>在操作成功完成的情况下和失败的情况下都将返回True。
        // 如果操作失败，{iMqttDeliveryToken.getException()}将*为非空。*</p>*
        try {
            String[] topics = iMqttDeliveryToken.getTopics();
            LOGGER.info("deliveryComplete[topic:{}] action has finished:{}",topics[0],iMqttDeliveryToken.isComplete());
            MqttMessage mqttMessage = iMqttDeliveryToken.getMessage();
            Executor executor = MqttTemplate.getMqttConfig().getExecutor();
            if(mqttMessage != null){
                String payload = new String(mqttMessage.getPayload());
                LOGGER.info("deliveryComplete==>Topic:{},Id:{},Qos:{},Retained:{},Message:{}",topics[0],mqttMessage.getId(),mqttMessage.getQos(),mqttMessage.isRetained(),payload);
                if (handlerContainer.containsKey(topics[0])){
                    if (executor != null){
                        CompletableFuture.runAsync(() -> sendDeliveryComplete(handlerContainer.get(topics[0]), iMqttDeliveryToken, topics[0], payload), executor);
                    }else {
                        sendDeliveryComplete(handlerContainer.get(topics[0]), iMqttDeliveryToken, topics[0], payload);
                    }
                }else {
                    Map<String, IMqttTopicMessageHandler<Object>> sendTopicMap = getSendTopic(topics[0]);
                    for (Map.Entry<String, IMqttTopicMessageHandler<Object>> entry : sendTopicMap.entrySet()) {
                        if (executor != null){
                            CompletableFuture.runAsync(() -> sendDeliveryComplete(entry.getValue(), iMqttDeliveryToken, topics[0], payload), executor);
                        }else {
                            sendDeliveryComplete(entry.getValue(), iMqttDeliveryToken, topics[0], payload);
                        }
                    }
                }
                /*if (handlerContainer.containsKey(topics[0])){
                    IMqttTopicMessageHandler<Object> targetClass = handlerContainer.get(topics[0]);
                    Class<?> genericClass = getGenericClass(targetClass);// 获取泛型
                    Object body = coverBody(payload, genericClass);
                    targetClass.deliveryComplete(topics[0], body,iMqttDeliveryToken);
                }*/
            }else{
                LOGGER.info("deliveryComplete[topic:{}]:mqttMessage=null",topics[0]);
            }
        }catch (Exception e){
            String[] topics = iMqttDeliveryToken.getTopics();
            LOGGER.error("MQTT deliveryComplete[topic:{}] Exception,error:{}",topics[0],e);
        }
    }

    /**
     * 消息体转换
     * @param msgContent
     * @param genericClass
     * @return
     * @throws IOException
     */
    private Object coverBody(String msgContent, Class<?> genericClass) throws IOException {
        Object body = null;
        if(isSendBody(msgContent)){
            LOGGER.info("MessageBody data:{}",msgContent);
            MessageBody messageBody = JSON.parseObject(msgContent,  MessageBody.class ) ;
            String chainID = messageBody.getChainID();
            //将链路全局id传入到ThreadLocal中
            MqttConttext.set(chainID);
            //获取泛型的类型
            if(messageBody.getData() instanceof String){
                body = JSON.parseObject(messageBody.getData().toString(), genericClass);
            }else {
                String msgJson =  JSON.toJSONString(messageBody.getData());
                body = JSON.parseObject(msgJson, genericClass) ;
            }
        }else{
            LOGGER.info("Not MessageBody type data:{}",msgContent);
            //非MessageBody---直接处理
            body = JSON.parseObject(msgContent, genericClass) ;
        }
        return body;
    }

    /**
     * 获取泛型数据类型(只能获取一级子类泛型，多级会报错)
     * @param object
     * @return
     */
    public Class<?> getGenericClass(Object object){
        //获取泛型的类型
        Type genericInterface = object.getClass().getGenericInterfaces()[0];
        ParameterizedType parameterizedType=(ParameterizedType) genericInterface;
        return (Class<?>)parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * 判断是否是框架提供的sendBody
     * @param msg
     * @return
     * @throws IOException
     */
    private boolean isSendBody(String msg) throws IOException {
        if(msg.contains("\"chainID\":")&&msg.contains("\"data\":")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取topic
     * @param topic
     * @return
     */
    private String getTopic(String topic) {
        if (topic == null || "".equals(topic)){
            return "";
        }
        for (String t : handlerContainer.keySet()) {
            if (MqttTopicMatcher.match(t, topic)) {
                return t;
            }
        }
        return "";
    }

    /**
     * 获取topic
     * @param topic
     * @return
     */
    private Map<String,IMqttTopicMessageHandler<Object>> getSendTopic(String topic) {
        Map<String, IMqttTopicMessageHandler<Object>> sendMap = new HashMap<>();
        if (topic == null || "".equals(topic)){
            return sendMap;
        }
        for (String t : handlerContainer.keySet()) {
            if (MqttTopicMatcher.match(t, topic)) {
                sendMap.put(t, handlerContainer.get(t));
            }
        }
        return sendMap;
    }

    /**
     * 发送消息
     * @param targetClass
     * @param topic
     * @param message
     * @param msgContent
     */
    private void sendMessageArrived(IMqttTopicMessageHandler<Object> targetClass,String topic, MqttMessage message, String msgContent) {
        try {
            Class<?> genericClass = getGenericClass(targetClass);// 获取泛型
            Object body = msgContent;
            if (JSON.isValid(msgContent)){
                body = coverBody(msgContent, genericClass);
            }
            targetClass.messageArrived(topic, body, message); //执行处理消息的逻辑
        } catch (Exception e) {
            LOGGER.error("Message distribution failed,[topic:{}],{}", topic,e);
        } finally {
            MqttConttext.remove();
        }
    }

    /**
     * 发送消息完成DeliveryComplete
     * @param iMqttDeliveryToken
     * @param topic
     * @param payload
     * @return
     * @throws IOException
     */
    private void sendDeliveryComplete(IMqttTopicMessageHandler<Object> targetClass,IMqttDeliveryToken iMqttDeliveryToken, String topic, String payload){
        try {
            Class<?> genericClass = getGenericClass(targetClass);// 获取泛型
            Object body = payload;
            if (JSON.isValid(payload)){
                body = coverBody(payload, genericClass);
            }
            targetClass.deliveryComplete(topic, body, iMqttDeliveryToken);
        } catch (Exception e) {
            LOGGER.error("Message sendDeliveryComplete failed,[topic:{}],{}", topic,e);
        }
    }
}
