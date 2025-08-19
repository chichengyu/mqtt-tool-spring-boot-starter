package com.mqtt.tool.core;

import com.mqtt.tool.body.MessageBody;

public abstract class MqttTemplateImpl implements IMqttTemplate{

    public abstract boolean publish(String topic, MessageBody data);

    public abstract boolean publish(String topic, Object data,boolean retained);

    public abstract boolean delayPublish(String topic, MessageBody data,int seconds);

    public abstract boolean delayPublish(String topic, MessageBody data,int seconds,boolean retained);

    public abstract boolean subscribe(String topic);

    public abstract <T>boolean subscribe(String topic,IMqttTopicMessageHandler<T> handler);

    public abstract boolean subscribe(String topic,boolean queue);

    public abstract <T>boolean subscribe(String topic,boolean queue,IMqttTopicMessageHandler<T> handler);

    public abstract boolean subscribe(String topic,boolean queue,boolean share);

    public abstract <T>boolean subscribe(String topic,boolean queue,boolean share,IMqttTopicMessageHandler<T> handler);

    public abstract boolean subscribe(String topic,boolean queue,boolean share,String group);

    public abstract <T>boolean subscribe(String topic,boolean queue,boolean share,String group,IMqttTopicMessageHandler<T> handler);

    public abstract boolean unSubscribe(String topic);

    public abstract boolean unSubscribe(String topic,boolean queue);

    public abstract boolean unSubscribe(String topic,boolean queue,boolean share);

    public abstract boolean unSubscribe(String topic,boolean queue,boolean share,String group);

    /*public boolean sendTemplate(String topic, Object data){
        return sendTemplate(topic,data,null);
    }

    public boolean sendTemplate(String topic, Object data,Integer seconds){
        String chainID = MqttConttext.get();
        if(MqttConttext.get() == null) {
            chainID = UUIDUtils.getUUID();
        }
        MessageBody messageBody = new MessageBody(data,chainID);
        if(seconds != null){
            return this.delayPublish(topic,messageBody,seconds);
        }else{
            return this.publish(topic,messageBody);
        }
    }*/

}
