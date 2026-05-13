package com.example.coreserver.service.socket;

import com.example.coreserver.entity.data.*;
import com.example.coreserver.handler.RadarDataHandler;
import com.example.coreserver.repository.data.ElectricInvestigationEntityRepository;
import com.example.coreserver.repository.data.PhotoelectricEntityRepository;
import com.example.coreserver.repository.data.RadarEntityRepository;
import com.example.coreserver.service.algorithm.AlgorithmGrpcClient;
import com.example.coreserver.service.business.GeofenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@ClientEndpoint
public class DataSaveClientHandler {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ObjectMapper objectMapper;
    private final RadarEntityRepository radarEntityRepository;
    private final ElectricInvestigationEntityRepository electricInvestigationEntityRepository;
    private final PhotoelectricEntityRepository photelectricEntityRepository;

    private static volatile DataSaveClientHandler webSocket; // 使用 volatile 关键字

    public DataSaveClientHandler(ObjectMapper objectMapper, RadarEntityRepository radarEntityRepository, ElectricInvestigationEntityRepository electricInvestigationEntityRepository, PhotoelectricEntityRepository photelectricEntityRepository) {
        this.objectMapper = objectMapper;
        this.radarEntityRepository = radarEntityRepository;
        this.electricInvestigationEntityRepository = electricInvestigationEntityRepository;
        this.photelectricEntityRepository = photelectricEntityRepository;
    }

    @PostConstruct
    public void init() {
        webSocket = this;
        log.info("DataSaveClientHandler initialized. webSocket: {}", webSocket);
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 解析原始WebSocket数据
            JsonNode originalData = mapper.readTree(message);

            saveData(originalData);

        } catch (Exception e) {
            log.error("Message processing failed in {}ms | Error: {}",
                    System.currentTimeMillis() - startTime,
                    e.getMessage(),
                    e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.warn("Connection closed: {} | Reason: {}",
                session.getId(),
                closeReason.getReasonPhrase());

        // 将 webSocket 置为 null，表示连接已关闭
        webSocket = null;
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        log.error("WebSocket error on session {}: {}",
                session != null ? session.getId() : "null",
                thr.getMessage(),
                thr);
    }

    /**
     * Save data to the database based on its type
     * @param data The data object to save
     */
    public void saveData(Object data) {
        try {
            if (data instanceof ObjectNode jsonNode) {
                String dataType = jsonNode.path("type").asText();
                String jsonStr = objectMapper.writeValueAsString(jsonNode);
//                saveMonthlyDroneData();
                switch (dataType) {
                    case "RADAR" -> saveRadarData(jsonNode, jsonStr);
                    case "ELECTRIC_UAV" -> saveElectricInvestigationData(jsonNode, jsonStr);
                    case "PE_UDP" ->
                            savePhotoelectricData(jsonNode, jsonStr, dataType);

                    default -> log.warn("Unrecognized data type for persistence: {}", dataType);
                }
            } else if (data instanceof RadarData radarData) {
                saveRadarData(radarData);
            } else if (data instanceof ElectricInvestigationData electricData) {
                saveElectricInvestigationData(electricData);
            } else if (data instanceof PhotoelectricData photoelectricData) {
                savePhotoelectricData(photoelectricData);
            } else {
                log.warn("Unsupported data type for persistence: {}", data.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error saving data to database: {}", e.getMessage(), e);
        }
    }

    /**
     * Save radar data from JSON node
     */
    private void saveRadarData(ObjectNode jsonNode, String jsonStr) throws JsonProcessingException {
        RadarEntity entity = RadarEntity.builder()
                .deviceId(jsonNode.path("deviceId").asText())
                .timestamp(LocalDateTime.now())
                .type("RADAR")
                .originalJson(jsonStr)
                .build();


        if (jsonNode.has("targets")) {
            entity.setTargetsJson(jsonNode.path("targets").toString());
        }

        radarEntityRepository.save(entity);
//        log.info("Saved radar data to database");
    }

    /**
     * Save radar data from RadarData object
     */
    private void saveRadarData(RadarData radarData) throws JsonProcessingException {
        RadarEntity entity = RadarEntity.builder()
                .deviceId(radarData.getDeviceId())
                .timestamp(radarData.getTimestamp() != null ? radarData.getTimestamp() : LocalDateTime.now())
                .targetsJson(radarData.getTargets() != null ? objectMapper.writeValueAsString(radarData.getTargets()) : null)
                .type("RADAR")
                .originalJson(objectMapper.writeValueAsString(radarData))
                .build();

        radarEntityRepository.save(entity);
//        log.info("Saved radar data object to database");
    }

    /**
     * Save electric investigation data from JSON node
     */
    private void saveElectricInvestigationData(ObjectNode jsonNode, String jsonStr) throws JsonProcessingException {
        ElectricInvestigationEntity entity = ElectricInvestigationEntity.builder()
                .timestamp(LocalDateTime.now())
                .type("ELECTRIC_UAV")
                .originalJson(jsonStr)
                .build();


        electricInvestigationEntityRepository.save(entity);
//        log.info("Saved electric investigation data to database");
    }

    /**
     * Save electric investigation data from ElectricInvestigationData object
     */
    private void saveElectricInvestigationData(ElectricInvestigationData electricData) throws JsonProcessingException {
        ElectricInvestigationEntity entity = ElectricInvestigationEntity.builder()
                .timestamp(LocalDateTime.now())
                .updateTime(electricData.getUpdateTime())
                .type("ELECTRIC_UAV")
                .originalJson(objectMapper.writeValueAsString(electricData))
                .build();

        electricInvestigationEntityRepository.save(entity);
//        log.info("Saved electric investigation data object to database");
    }

    /**
     * Save photoelectric data from JSON node
     */
    private void savePhotoelectricData(ObjectNode jsonNode, String jsonStr, String dataType) {
        PhotoelectricEntity entity = PhotoelectricEntity.builder()
                .deviceId(jsonNode.path("deviceId").asText())
                .timestamp(LocalDateTime.now())
                .type(dataType)
                .originalJson(jsonStr)
                .build();

        // Extract more properties if they exist

        photelectricEntityRepository.save(entity);
//        log.info("Saved photoelectric data ({}) to database", dataType);
    }

    /**
     * Save photoelectric data from PhotoelectricData object
     */
    private void savePhotoelectricData(PhotoelectricData photoelectricData) throws JsonProcessingException {
        PhotoelectricEntity entity = PhotoelectricEntity.builder()
                .deviceId(photoelectricData.getDeviceId())
                .timestamp(photoelectricData.getTimestamp() != null ? photoelectricData.getTimestamp() : LocalDateTime.now())
                .type("PE_UDP")  // Default type
                .originalJson(objectMapper.writeValueAsString(photoelectricData))
                .build();

        photelectricEntityRepository.save(entity);
//        log.info("Saved photoelectric data object to database");
    }
}
