package com.mqtt.tool.config;

import com.mqtt.tool.MqttTemplate;
import com.mqtt.tool.core.MqttCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ApplicationObjectSupport;

public class ContextConfig extends ApplicationObjectSupport {
    private static Logger LOGGER = LoggerFactory.getLogger(ContextConfig.class);

    public static ConfigurableApplicationContext applicationContext;

    @Override
    public void initApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ContextConfig.applicationContext = (ConfigurableApplicationContext)applicationContext;
            this.init();
        }
    }

    @Bean
    private MqttCallback mqttCallback(){
        return new MqttCallback();
    }

    public void init() {
        MqttTemplate.init(applicationContext,mqttCallback());
        /*MqttConfig mqttConfig = null;
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
            client.setCallback(mqttCallback());
            client.connect(mqttConnectOptions(mqttConfig));
            MqttTemplate.setClient(client);
            LOGGER.info("MQTT client init finish");
        } catch (MqttException e) {
            LOGGER.error("MQTT connection failed",e);
        }*/
    }

    /**
     * MqttConnectOptions config
     * @param mqttConfig
     * @return
     */
    /*private MqttConnectOptions mqttConnectOptions(MqttConfig mqttConfig) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttConfig.getUsername());
        options.setPassword(mqttConfig.getPassword().toCharArray());
        options.setAutomaticReconnect(mqttConfig.getAutomaticReconnect());//是否自动重新连接
        options.setCleanSession(mqttConfig.getCleanSession());//是否清除之前的连接信息
        options.setConnectionTimeout(mqttConfig.getConnectionTimeout());//连接超时时间
        options.setKeepAliveInterval(mqttConfig.getKeepAliveInterval());//心跳
        options.setMqttVersion(mqttConfig.getVersion());//设置mqtt版本
        return options;
    }*/
}
