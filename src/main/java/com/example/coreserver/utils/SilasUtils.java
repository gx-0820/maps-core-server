package com.example.coreserver.utils;

import com.example.coreserver.entity.data.RadarTarget;
import com.example.coreserver.entity.silas.*;
import com.example.coreserver.service.device.SilasService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Set;

/**
 * @author zhan
 */
@Component
@Slf4j
public class SilasUtils {

    @Autowired
    private SilasService silasService;

    @Value("${var.radar.lat}")
    public double lat;

    @Value("${var.radar.lon}")
    public double lon;

    // !!! 以下所有配置参数值以协议为准 !!!

    public static final boolean CLEAN_SESSION = false;
    public static final int QOS = 0;
    public static final int CONNECT_TIMEOUT = 10;
    public static final String CLIENT_ID = MqttClient.generateClientId() + System.currentTimeMillis();

    public SilasUtils() {
    }

    public static MqttCallback buildMqttCallback() {
        return new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                log.error(MessageFormat.format("【MQTT】connection lost, cause: {0}", cause));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                // Add codes here to process receive message.
                log.debug(MessageFormat.format("received message from topic: {0}, payload: {1}", topic, message.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    log.debug(MessageFormat.format("【成功投递到MQTT】 topic= {0}",
                            Arrays.asList(token.getTopics())));
                } catch (Exception e) {
                    log.debug(MessageFormat.format("【投递消息到MQTT】occur exception, cause: {0}", e.getMessage()));
                }
            }
        };
    }

    public static MqttConnectOptions buildMqttConnectOptions(String username, String password, boolean cleanSession,
                                                             int connectTimeout) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectTimeout);
        options.setKeepAliveInterval(connectTimeout);
        return options;
    }

    public static void println(String message) {
        System.out.println(MessageFormat.format("{0} {1} ", message, now()));
    }

    private static String now() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return simpleDateFormat.format(new Date()) + "\t";
    }

    public static String readFile(String filePath) {
        StringBuilder builder = new StringBuilder("");
        byte[] buffer = new byte[1024];
        int count;
        File file = new File(filePath);
        try {
            InputStream inputStream = new FileInputStream(file);
            while (-1 != (count = inputStream.read(buffer))) {
                builder.append(new String(buffer, 0, count));
            }
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return builder.toString();
    }

    public static boolean validate(String content) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema jsonSchema = factory.getSchema(SilasUtils.class.getResourceAsStream("/schema.json"));
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(content);
            Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
            return errors.isEmpty();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static Double formatDouble2(double value) {
        return Double.parseDouble(String.format("%.2f", value));
    }

    public static Double formatDouble1(double value) {
        return Double.parseDouble(String.format("%.1f", value));
    }

    public static Double formatDouble7(double value) {
        return Double.parseDouble(String.format("%.7f", value));
    }

    public void sendRadarTargetToSilas(RadarTarget radarTarget) throws JsonProcessingException {
        Sensor sensor = Sensor.builder()
                .sensorManufacturerID("91110000710924910P")     //企业信用代码
                .sensorSN("NPU203I2013I02302")      //设备SN码
                .sensorOperatorInfo(SensorOperatorInfo.builder()
                        .entityID("91110000710924910P")
                        .entityName("MAPS")
                        .entityType("PublicInst")
                        .phone("+86-15309252676")
                        .build())
                .sensorLocation(SensorLocation.builder()
                        .latitude(formatDouble7(lat))
                        .longitude(formatDouble7(lon))
                        /*
                        根据实际情况修改
                         */
                        .height(52.21)//后期在前端输入雷达架设高度
                        .build())
                .sensorPosture(SensorPosture.builder()
                        .azimuth(formatDouble2(radarTarget.getAzimuth2())) // 正北0°
                        .elevation(formatDouble2(radarTarget.getPitch()))
                        .twist(null)
                        .build())
                .sensorExtra(SensorExtra.builder()
                        /*
                        根据实际情况修改
                         */
                        .sensorCategory("mmWaveRadar")
                        .build())
                .build();

        long sampleTime = Timestamp.valueOf(radarTarget.getCreateTime()).getTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        Random random = new Random();
        int randomNumber = random.nextInt(90000000) + 10000000;

        PerceivedStatus perceivedStatus = PerceivedStatus.builder()
                .trackID("MAPS-" + radarTarget.getTargetId() + "-" + LocalDate.now().format(formatter))
                .objectID("MAPS-" + radarTarget.getTargetId())
                .sampleTime(sampleTime - 12)
                // ISO 8601持续时间格式
                //后续根据实际情况修改
                .trackDuration("PT4M12S")
                // 暂时默认巡航
                .flightStage("Cruising") // 做成动态的
                // sensor用距离角度
                .objectPosition(SensorPosition.builder()
                        .positionBase("Sensor")
                        .range(formatDouble2(radarTarget.getRange()))
                        .azimuth(formatDouble1(radarTarget.getAzimuth2() + 180.0))
                        .elevation(formatDouble1(radarTarget.getPitch() + 180.0))
                        .build())
                .objectPosition(GroundPosition.builder()
                        .positionBase("Ground")
                        .longitude(formatDouble7(radarTarget.getTargetLon()))
                        .latitude(formatDouble7(radarTarget.getTargetLat()))
                        .height(formatDouble2(radarTarget.getAltitude() + 52.21))
                        .build())
                .objectVelocity(SensorVelocity.builder()
                        .velocityBase("Sensor")
                        .radialVelocity(formatDouble1(radarTarget.getSpeed()))
                        .build())
                .objectStatusExtra(ObjectStatusExtra.builder()
                        .aircraft(Aircraft.builder()
                                .aircraftState("NoRecord")
                                .aircraftType("Other")
                                .aircraftCategory("MicroUAV")
                                .aircraftName(null)
                                .aircraftModel(null)
                                .manufacturerName(null)
                                .aircraftEmptyWeight(null)
                                .aircraftEmptyWeightWithBattery(null)
                                .actualPayloadWeight(null)
                                .maxTakeoffWeight(null)
                                .SN(null)
                                .build())
                        .manipulator(Manipulator.builder()
                                .longitude(formatDouble7(lon))
                                .latitude(formatDouble7(lat))
                                .build())
                        .build())
                .build();

        SilasData silasData = SilasData.builder()
                .sensor(sensor)
                .CRS("WGS84")
                .heightDesc(HeightDesc.builder()
                        .heightType("MSL")
                        .baseModel("EGM2008")
                        .build())
                .perceivedStatus(perceivedStatus)
                .build();

        SilasEntity silasEntity = SilasEntity.builder()
                .dataType("PerceivedStatusUpload")
                .version("1.0")
                .source("JXGY")
                .uploadTime(System.currentTimeMillis() - 10)
                .silasItem(silasData)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(silasEntity);
//        System.out.println(message);
        silasService.sendMqttMessage(message);
    }
}

