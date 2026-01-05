# mqtt-tool-spring-boot-starter
<p align="left">
    <a href="https://github.com/chichengyu/task-spring-boot-starter">
        <img src="https://img.shields.io/badge/%E4%BD%9C%E8%80%85-%E5%B0%8F%E6%B1%A0-%23129e50" alt="MIT License" />
    </a>
    <a href="https://github.com/chichengyu/mqtt-tool-spring-boot-starter">
        <img src="https://img.shields.io/badge/last version-1.2.1-green" alt="version-1.2.1" />
    </a>
</p>

#### 介绍
基于mqtt原生依赖封装的工具

#### 安装
引入坐标
```
<dependency>
    <groupId>io.github.chichengyu</groupId>
    <artifactId>mqtt-tool-spring-boot-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

#### 使用
###### 1.创建MqttClientConfig配置文件
内容如下：
```
@Configuration
public class MqttClientConfig {

    @Bean
    public com.mqtt.tool.config.MqttConfig mqttConfig(){
        com.mqtt.tool.config.MqttConfig mqttConfig = new com.mqtt.tool.config.MqttConfig();
        mqttConfig.setHost("127.0.0.1");
        mqttConfig.setUsername("test");
        mqttConfig.setPassword("123456");
        mqttConfig.setClientId("xxxxxxxxxxxx");
        mqttConfig.setExecutor("线程池");
        return mqttConfig;
    }
}
```
###### 2.创建消息处理器
实现接口 `IMqttTopicMessageHandler`的消息处理器，在项目启动后，会通过`@MqttTopic`自动订阅主题，
创建文件`TestMqttTopicMessage`实现接口` IMqttTopicMessageHandler `，内容如下
```
@Component
@MqttTopic("test/abc")
public class TestMqttTopicMessage implements IMqttTopicMessageHandler<TesVo> {

    @Override
    public void messageArrived(String topic, TesVo body) throws Exception {
        System.out.println("接收到消息了  Test2MqttTopicMessage");
    }

    @Override
    public void deliveryComplete(String topic, TesVo params) {

    }
}
```
`TesVo`是定义的dto，会自动转换类型。
###### 3.发消息
提供类`MqttTemplate`，静态方法进行发送、订阅、取消订阅消息
```
@RestController
@RequestMapping("/test")
public class Test {

    @GetMapping("/send")
    public String tets(@RequestParam String topic){
        TesVo vo = new TesVo();
        vo.setName("小三");
        vo.setAge(18);
        MqttTemplate.getMqttTemplate().publish(topic,vo);
       // MqttTemplate.getMqttTemplate().delayPublish(topic+"/a",vo,20);
        return "ok";
    }
}
```
使用简单。