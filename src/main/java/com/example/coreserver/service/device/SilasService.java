package com.example.coreserver.service.device;

import com.example.coreserver.config.MqttConfig;
import com.example.coreserver.config.SilasConfig;
import com.example.coreserver.utils.SilasUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class SilasService {

    @Autowired
    private SilasConfig silasConfig;

    private String BROKER;

    private String TOPIC;

    private String USERNAME;

    private String PASSWORD;

    public MqttClient client;

    @Autowired
    private MqttConfig mqttConfig;

    @PostConstruct
    public void init() throws MqttException {
        if (mqttConfig.getStatus() == 0) {
            return;
        }
        if (client != null && client.isConnected()) {
            return;
        }

        BROKER = silasConfig.getBroker();
        TOPIC = silasConfig.getTopic();
        USERNAME = silasConfig.getUsername();
        PASSWORD = silasConfig.getPassword();

        // 1 初始化客户端
        client = new MqttClient(BROKER, SilasUtils.CLIENT_ID, new MemoryPersistence());
        client.setCallback(SilasUtils.buildMqttCallback());
        // 2 准备连接参数
        MqttConnectOptions options = SilasUtils.buildMqttConnectOptions(USERNAME, PASSWORD, SilasUtils.CLEAN_SESSION,
                SilasUtils.CONNECT_TIMEOUT);

//        try {
//            String caCrtFile = SilasService.class.getResource("/emqxsl-ca.crt").getPath();
//            options.setSocketFactory(SilasSSLUtils.getSingleSocketFactory(caCrtFile));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        options.setMaxInflight(10);
        options.setKeepAliveInterval(30);


        // 3 连接
        client.connect(options);
        if (!client.isConnected()) {
            log.error("Failed to connect to MQTT broker: " + client.getServerURI());
            return;
        }

//        client.subscribe(TOPIC, SilasUtils.QOS);

    }

    public void close() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
            client.close();
        }
    }


    public void sendMqttMessage(String msgValue) {
        try {

            // !!!此处若是实际生产代码，请换成真实的飞行动态json数据!!!
            // String msgValue = SilasUtils.readFile("C:\\Users\\zhn\\Desktop\\205\\MQTT\\mqtt-demo\\PerceivedObjectStatus\\Java\\mqtt\\src\\main\\resources\\sample-data.json");
            // 4 使用JsonSchema校验报文格式
//            boolean validateResult = SilasUtils.validate(msgValue);
//            if (!validateResult) {
//                log.error("报文格式有误, 请检查后重试");
//                return;
//            }
            // 5 创建消息并发送消息
            MqttMessage message = new MqttMessage(msgValue.getBytes());
            message.setQos(SilasUtils.QOS);
            client.publish(TOPIC, message);
            log.debug("Message published, topic: " + TOPIC + "message content: " + msgValue);
            // 6 关闭连接和客户端
//            client.disconnect();
//            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
