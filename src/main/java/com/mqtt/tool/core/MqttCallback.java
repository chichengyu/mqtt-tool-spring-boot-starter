package com.mqtt.tool.core;


import com.alibaba.fastjson2.JSON;
import com.mqtt.tool.MqttConttext;
import com.mqtt.tool.MqttTemplate;
import com.mqtt.tool.annotation.MqttTopic;
import com.mqtt.tool.body.MessageBody;
import com.mqtt.tool.config.ContextConfig;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class MqttCallback implements MqttCallbackExtended {
    private static Logger LOGGER = LoggerFactory.getLogger(MqttCallback.class);

    public static ConcurrentHashMap<String,IMqttTopicMessageHandler<Object>> container = new ConcurrentHashMap<>();

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
                        container.put(mqttTopic.value(),targetClass);
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
        String msgContent = new String(message.getPayload());//获取emq发过来的消息体
        LOGGER.info("messageArrived==>Topic:{},Id:{},Qos:{},Retained:{},Message:{}",topic,message.getId(),message.getQos(),message.isRetained(),msgContent);
        if (container.containsKey(topic)){
            try {
                IMqttTopicMessageHandler<Object> targetClass = container.get(topic);
                Class<?> genericClass = getGenericClass(targetClass);// 获取泛型
                Object body = coverBody(msgContent, genericClass);
                targetClass.messageArrived(topic, body); //执行处理消息的逻辑
            } catch (Exception e) {
                LOGGER.error("Message distribution failed,{}",e);
            } finally {
                MqttConttext.remove();
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
        try {
            String[] topics = iMqttDeliveryToken.getTopics();
            MqttMessage mqttMessage = iMqttDeliveryToken.getMessage();
            if(mqttMessage != null){
                String payload = new String(mqttMessage.getPayload());
                LOGGER.info("deliveryComplete==>Topic:{},Id:{},Qos:{},Retained:{},Message:{}",topics[0],mqttMessage.getId(),mqttMessage.getQos(),mqttMessage.isRetained(),payload);
                LOGGER.info("deliveryComplete:{}",payload);
                if (container.containsKey(topics[0])){
                    IMqttTopicMessageHandler<Object> targetClass = container.get(topics[0]);
                    Class<?> genericClass = getGenericClass(targetClass);// 获取泛型
                    Object body = coverBody(payload, genericClass);
                    targetClass.deliveryComplete(topics[0], body);
                }
            }else{
                LOGGER.info("deliveryComplete:mqttMessage=null");
            }
        }catch (Exception e){
            LOGGER.error("MQTT deliveryComplete Exception,error:{}",e);
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
            LOGGER.info("MessageBody data----");
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
            LOGGER.info("Not MessageBody type data");
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
}
