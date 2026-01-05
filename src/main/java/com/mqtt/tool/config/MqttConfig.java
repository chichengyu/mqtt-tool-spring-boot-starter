package com.mqtt.tool.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MqttConfig{

    /**
     * 主机
     */
    private String host = "127.0.0.1";

    /**
     * 端口
     */
    private int port = 1883;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 如果不设置就默认拼接 tcp://127.0.0.1:1883，设置了就取当前serverURI
     */
    private String serverURI;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 心跳
     */
    private int keepAliveInterval = 60;

    /**
     * 连接超时时间
     */
    private int connectionTimeout = 120;

    /**
     * 最大重连延迟128秒
     */
    private int maxReconnectDelay = 128000;

    /**
     * 是否启用https主机名
     */
    private boolean httpsHostnameVerificationEnabled = true;

    /**
     * 自定义头
     */
    private Properties customWebSocketHeaders = null;

    /**
     * 是否自动重新连接
     */
    private Boolean automaticReconnect = true;

    /**
     * 是否清除之前的连接信息
     */
    private Boolean cleanSession = true;

    /**
     * 是否手动ack，如果为true,则 qos=0
     */
    private Boolean ack = false;

    /**
     * 版本 默认 MqttConnectOptions.MQTT_VERSION_3_1_1
     */
    private int version = MqttConnectOptions.MQTT_VERSION_3_1_1;

    /**
     * 线程池
     */
    private Executor executor = null;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerURI() {
        return serverURI;
    }

    public void setServerURI(String serverURI) {
        this.serverURI = serverURI;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxReconnectDelay() {
        return maxReconnectDelay;
    }

    public void setMaxReconnectDelay(int maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
    }

    public boolean isHttpsHostnameVerificationEnabled() {
        return httpsHostnameVerificationEnabled;
    }

    public void setHttpsHostnameVerificationEnabled(boolean httpsHostnameVerificationEnabled) {
        this.httpsHostnameVerificationEnabled = httpsHostnameVerificationEnabled;
    }

    public Properties getCustomWebSocketHeaders() {
        return customWebSocketHeaders;
    }

    public void setCustomWebSocketHeaders(Properties customWebSocketHeaders) {
        this.customWebSocketHeaders = customWebSocketHeaders;
    }

    public Boolean getAutomaticReconnect() {
        return automaticReconnect;
    }

    public void setAutomaticReconnect(Boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    public Boolean getCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(Boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public Boolean getAck() {
        return ack;
    }

    public void setAck(Boolean ack) {
        this.ack = ack;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
