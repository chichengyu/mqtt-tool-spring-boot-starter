package com.mqtt.tool.body;

/**
 * 消息body
 */
public class MessageBody {

    /**
     * content内容
     */
    private Object data;

    /**
     * 是否发送原始数据(只包含发送的数据，不包含消息链id/qos/reatined等等)
     */
    private boolean rawDataSend = true;

    /**
     * 消息链id
     */
    private String chainID;

    /**
     * 是否保留消息
     */
    private boolean retained = false;

    /**
     * 消息质量qos,范围 0 / 1 / 2
     */
    private Integer qos = 0;

    /**
     * 消息id,可不设置
     */
    private Integer id;

    public MessageBody() {
    }

    public MessageBody(Object data, String chainID) {
        this.data = data;
        this.chainID = chainID;
    }
    public MessageBody(Object data, String chainID, boolean retained, Integer qos, Integer id) {
        this.data = data;
        this.chainID = chainID;
        this.retained = retained;
        this.qos = qos;
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isRawDataSend() {
        return rawDataSend;
    }

    public void setRawDataSend(boolean rawDataSend) {
        this.rawDataSend = rawDataSend;
    }

    public String getChainID() {
        return chainID;
    }

    public void setChainID(String chainID) {
        this.chainID = chainID;
    }

    public boolean isRetained() {
        return retained;
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
