package com.mqtt.tool;

import com.alibaba.fastjson2.JSON;
import com.mqtt.tool.body.MessageBody;
import com.mqtt.tool.config.MqttConfig;
import com.mqtt.tool.core.IMqttTopicMessageHandler;
import com.mqtt.tool.core.MqttCallback;
import com.mqtt.tool.core.MqttTemplateImpl;
import com.mqtt.tool.util.UUIDUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class MqttTemplate extends MqttTemplateImpl {
    private static Logger LOGGER = LoggerFactory.getLogger(MqttTemplate.class);

    private static MqttClient mqttClient;

    private static MqttConfig mqttConfig;

    private static MqttTemplate mqttTemplate;

    private static byte[] lock = new byte[0];

    public static MqttTemplate getMqttTemplate() {
        if (mqttTemplate == null){
            synchronized (lock){
                if (mqttTemplate == null){
                    mqttTemplate = new MqttTemplate();
                }
            }
        }
        return mqttTemplate;
    }

    /**
     * 发送消息
     * @param topic
     * @param data
     * @return
     */
    @Override
    public boolean publish(String topic, Object data) {
        MessageBody messageBody = new MessageBody();
        messageBody.setData(data);
        return publish(topic,messageBody);
    }

    /**
     * 发送消息
     * @param topic 主题
     * @param data body
     * @param retained 是否保留消息
     * @return
     */
    @Override
    public boolean publish(String topic, Object data,boolean retained){
        MessageBody messageBody = new MessageBody();
        messageBody.setData(data);
        messageBody.setRetained(retained);
        return publish(topic,messageBody);
    }

    /**
     * 发送消息
     * @param topic 主题
     * @param messageBody body
     * @return
     */
    public boolean publish(String topic, MessageBody messageBody) {
        String payload = "";
        try {
            if (!messageBody.isRawDataSend()){
                String chainID = MqttConttext.get();
                if(MqttConttext.get() == null) {
                    chainID = UUIDUtils.getUUID();
                }
                messageBody.setChainID(chainID);
                payload = JSON.toJSONString(messageBody);
            }else {
                payload = JSON.toJSONString(messageBody.getData());
            }
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(payload.getBytes());
            mqttMessage.setQos(messageBody.getQos());
            if(mqttConfig.getAck()){
                mqttMessage.setQos(0);
            }
            mqttMessage.setRetained(messageBody.isRetained());
            if (messageBody.getId() != null){
                mqttMessage.setId(messageBody.getId());
            }
            mqttClient.publish(topic,mqttMessage);
            LOGGER.info("publish topic successful:{}",topic);
            return true;
        } catch (MqttException e) {
            LOGGER.error("MQTT publish MqttException error:{}",e);
            return false;
        }
    }

    /**
     * 发送延迟消息
     * @param topic 主题
     * @param data 数据体
     * @param seconds 延迟时间(秒)
     * @return
     */
    @Override
    public boolean delayPublish(String topic, Object data, int seconds) {
        return this.publish( "$delayed/"+seconds+"/"+topic, data);
    }

    /**
     * 发送延迟消息
     * @param topic 主题
     * @param data 数据体
     * @param seconds 延迟时间(秒)
     * @return
     */
    @Override
    public boolean delayPublish(String topic, MessageBody data, int seconds) {
        return this.publish( "$delayed/"+seconds+"/"+topic, data);
    }

    /**
     * 发送延迟消息
     * @param topic 主题
     * @param data 数据体
     * @param seconds 延迟时间(秒)
     * @param retained 是否保留消息
     * @return
     */
    @Override
    public boolean delayPublish(String topic, MessageBody data, int seconds,boolean retained) {
        return this.publish( "$delayed/"+seconds+"/"+topic, data,retained);
    }

    //==========================================subscribe================================================
    /**
     * 订阅主题
     * @param topic
     * @return
     */
    public boolean subscribe(String topic) {
        return subscribe(topic,false,false,"");
    }

    /**
     * 订阅主题
     * @param topic
     * @return
     */
    public <T>boolean subscribe(String topic,IMqttTopicMessageHandler<T> handler) {
        return subscribe(topic,false,false,"",handler);
    }

    /**
     * 订阅主题(无组group)
     * @param topic
     * @return
     */
    @Override
    public boolean subscribe(String topic,boolean queue) {
        return subscribe(topic,queue,false,"");
    }

    /**
     * 订阅主题(无组group)
     * @param topic
     * @return
     */
    @Override
    public <T>boolean subscribe(String topic,boolean queue,IMqttTopicMessageHandler<T> handler) {
        return subscribe(topic,queue,false,"",handler);
    }

    /**
     * 订阅主题(有组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public boolean subscribe(String topic,boolean queue,boolean share) {
        return subscribe(topic,queue,share,"");
    }

    /**
     * 订阅主题(有组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public <T>boolean subscribe(String topic,boolean queue,boolean share,IMqttTopicMessageHandler<T> handler) {
        return subscribe(topic,queue,share,"",handler);
    }

    /**
     * 订阅主题(有组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public boolean subscribe(String topic,boolean queue,boolean share,String group) {
        return subscribe(topic,queue,share,group,null);
    }

    /**
     * 订阅主题(有组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T>boolean subscribe(String topic,boolean queue,boolean share,String group,IMqttTopicMessageHandler<T> handler) {
        LOGGER.info("start subscribe topic:{}",topic);
        try {
            String topicFilter = getTopic(topic, queue, share,group);
            mqttClient.subscribe(topicFilter);
            if (handler != null){
                if (MqttCallback.getHandlerContainer().containsKey(topic)){
                    LOGGER.info("topic:{} Already subscribed, IMqttTopicMessageHandler will overwrite",topic);
                }
                MqttCallback.getHandlerContainer().put(topic,(IMqttTopicMessageHandler<Object>)handler);
            }
            LOGGER.info("subscribe topic:{} successful",topic);
            return true;
        } catch (MqttException e) {
            LOGGER.error("emq connect error", e);
            return false;
        }
    }

    /**
     * 取消订阅(无组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public boolean unSubscribe(String topic) {
        return unSubscribe(topic,false,false,"");
    }

    /**
     * 取消订阅(无组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public boolean unSubscribe(String topic,boolean queue) {
        return unSubscribe(topic,queue,false,"");
    }

    /**
     * 取消订阅(有组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public boolean unSubscribe(String topic,boolean queue,boolean share) {
        return unSubscribe(topic,queue,share,"");
    }

    /**
     * 取消订阅(有组group，共享消息)
     * @param topic
     * @return
     */
    @Override
    public boolean unSubscribe(String topic,boolean queue,boolean share,String group) {
        LOGGER.info("start unSubscribe topic:{}",topic);
        //和EMQ连接成功后根据配置自动订阅topic
        try {
            String mqttTopic = getTopic(topic, queue, share, group);
            LOGGER.info(">>>>>>>>>>>>>>subscribe topic:" + mqttTopic);
            mqttClient.unsubscribe(mqttTopic);
            IMqttTopicMessageHandler<Object> rs = MqttCallback.getHandlerContainer().remove(topic);
            if (rs != null){
                LOGGER.info("topic:{} IMqttTopicMessageHandler Removed already",topic);
            }
            LOGGER.info("unSubscribe topic:{} successful",topic);
            return true;
        } catch (MqttException e) {
            LOGGER.error("emq connect error", e);
            return false;
        }
    }

    /**
     * 处理topic
     * @param topic
     * @param queue
     * @param share
     * @return
     */
    public static String getTopic(String topic, boolean queue, boolean share,String group) {
        String topicFilter = topic;
        if(queue){//如果没有群组
            topicFilter = "$queue/" + topic;
        }
        if (share){
            topicFilter = "$share/"+ ((group != null && !"".equals(group) ? group : "default")) + "/"+  topic;
        }
        return topicFilter;
    }

    /**
     * client init
     * @param applicationContext
     * @param mqttCallback
     */
    public static void init(ConfigurableApplicationContext applicationContext, MqttCallback mqttCallback) {
        MqttConfig mqttConfig = null;
        if (applicationContext != null){
            mqttConfig = applicationContext.getBean(com.mqtt.tool.config.MqttConfig.class);
            MqttTemplate.setMqttConfig(mqttConfig);
        }
        String serverURI = String.format("tcp://%s:%s",mqttConfig.getHost(),mqttConfig.getPort());
        if (mqttConfig.getServerURI() != null && !"".equals(mqttConfig.getServerURI())){
            serverURI = mqttConfig.getServerURI();
        }
        try {
            MqttClient client = new MqttClient(
                    serverURI,
                    mqttConfig.getClientId(),
                    new MemoryPersistence());
            client.setManualAcks(mqttConfig.getAck()); //设置手动消息接收确认
            client.setCallback(mqttCallback);
            client.connect(mqttConnectOptions(mqttConfig));
            MqttTemplate.setClient(client);
            LOGGER.info("MQTT client init finish");
        } catch (MqttException e) {
            LOGGER.error("MQTT connection failed",e);
        }
    }

    /**
     * MqttConnectOptions config
     * @param mqttConfig
     * @return
     */
    public static MqttConnectOptions mqttConnectOptions(MqttConfig mqttConfig) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttConfig.getUsername());
        options.setPassword(mqttConfig.getPassword().toCharArray());
        options.setAutomaticReconnect(mqttConfig.getAutomaticReconnect());//是否自动重新连接
        options.setCleanSession(mqttConfig.getCleanSession());//是否清除之前的连接信息
        options.setConnectionTimeout(mqttConfig.getConnectionTimeout());//连接超时时间
        options.setKeepAliveInterval(mqttConfig.getKeepAliveInterval());//心跳
        options.setMaxReconnectDelay(mqttConfig.getMaxReconnectDelay());//最大重连延迟128秒
        options.setHttpsHostnameVerificationEnabled(mqttConfig.isHttpsHostnameVerificationEnabled());//是否启用https主机名
        options.setMqttVersion(mqttConfig.getVersion());//设置mqtt版本
        if (mqttConfig.getCustomWebSocketHeaders() != null){
            options.setCustomWebSocketHeaders(mqttConfig.getCustomWebSocketHeaders());
        }
        return options;
    }

    public static MqttClient getMqttClient() {
        return mqttClient;
    }

    public static MqttConfig getMqttConfig() {
        return mqttConfig;
    }

    public static void setClient(MqttClient client) {
        MqttTemplate.mqttClient = client;
    }

    public static void setMqttConfig(MqttConfig mqttConfig) {
        MqttTemplate.mqttConfig = mqttConfig;
    }
}
