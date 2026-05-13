package com.example.coreserver.controller;

import com.example.coreserver.config.MybatisPlusConfig;
import com.example.coreserver.service.business.TargetQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = TargetControllerIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc(addFilters = false)
class TargetControllerIntegrationTest {

    private record RadarSample(String targetId, String date, String minute, Integer targetType) {
    }

    private record TdoaSample(String targetId, String date, String minute, Integer targetType) {
    }

    private record FusionSample(String targetId, String date, String minute, String targetType) {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRadarTargetListAndTrajectoryFromDatabase() throws Exception {
        RadarSample sample = queryRadarSample();
        assertNotNull(sample);

        String listResponse = mockMvc.perform(get("/api/targets/radar")
                        .param("date", sample.date())
                        .param("startTime", sample.minute())
                        .param("endTime", sample.minute())
                        .param("targetType", String.valueOf(sample.targetType()))
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode listRoot = objectMapper.readTree(listResponse);
        JsonNode records = listRoot.path("data").path("records");
        assertFalse(records.isEmpty());
        JsonNode matchedRecord = findRecordByTargetId(records, sample.targetId());
        assertNotNull(matchedRecord);
        assertSuccessEnvelope(listRoot, 1, 20);
        assertFieldsPresent(matchedRecord,
                "targetId", "timestampBegin", "timestampEnd", "duration", "recordCount");

        String[] parts = sample.targetId().split("_", 2);
        assertFalse(sample.targetId().isBlank());

        String trajectoryResponse = mockMvc.perform(get("/api/targets/radar/{targetId}/trajectory", sample.targetId())
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andExpect(jsonPath("$.data.records[0].targetBatch").value(Long.parseLong(parts[0])))
                .andExpect(jsonPath("$.data.records[0].targetId").value(Integer.parseInt(parts[1])))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode trajectoryRoot = objectMapper.readTree(trajectoryResponse);
        JsonNode trajectoryRecords = trajectoryRoot.path("data").path("records");
        assertSuccessEnvelope(trajectoryRoot, 1, 10);
        assertFieldsPresent(trajectoryRecords.get(0),
                "id", "timestamp", "deviceId", "targetBatch", "targetId",
                "range", "azimuth2", "pitch", "speed", "altitude",
                "targetLat", "targetLon", "targetType");
        assertDescending(trajectoryRecords, "timestamp");
    }

    @Test
    void shouldReturnTdoaTargetListAndTrajectoryFromDatabase() throws Exception {
        TdoaSample sample = queryTdoaSample();
        assertNotNull(sample);

        String listResponse = mockMvc.perform(get("/api/targets/tdoa")
                        .param("date", sample.date())
                        .param("startTime", sample.minute())
                        .param("endTime", sample.minute())
                        .param("targetType", String.valueOf(sample.targetType()))
                        .param("pageNum", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode listRoot = objectMapper.readTree(listResponse);
        JsonNode records = listRoot.path("data").path("records");
        assertFalse(records.isEmpty());
        JsonNode matchedRecord = findRecordByTargetId(records, sample.targetId());
        assertNotNull(matchedRecord);
        assertSuccessEnvelope(listRoot, 1, 20);
        assertFieldsPresent(matchedRecord,
                "targetId", "uavModel", "timestampBegin", "timestampEnd", "duration", "recordCount");

        String[] parts = sample.targetId().split("_", 2);
        assertFalse(sample.targetId().isBlank());

        String trajectoryResponse = mockMvc.perform(get("/api/targets/tdoa/{targetId}/trajectory", sample.targetId())
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode trajectoryRoot = objectMapper.readTree(trajectoryResponse);
        JsonNode trajectoryRecords = trajectoryRoot.path("data").path("records");
        JsonNode firstRecord = trajectoryRoot.path("data").path("records").get(0);
        assertNotNull(firstRecord);
        assertSuccessEnvelope(trajectoryRoot, 1, 10);
        assertFieldsPresent(firstRecord,
                "id", "timestamp", "target_batch", "uav_id", "uav_model",
                "trace_id", "uav_lon", "uav_lat", "uav_alt", "velocity",
                "frequency", "white_list_id", "target_type", "uav_azimuth", "uav_distance");
        assertEquals(parts[0], firstRecord.path("uav_id").asText());
        assertEquals(parts[1], firstRecord.path("trace_id").asText());
        assertDescending(trajectoryRecords, "timestamp");
    }

    @Test
    void shouldReturnFusionTargetListAndTrajectoryFromDatabase() throws Exception {
        FusionSample sample = queryFusionSample();
        if (sample == null) {
            String emptyListResponse = mockMvc.perform(get("/api/targets/fusion")
                            .param("pageNum", "1")
                            .param("pageSize", "20"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode emptyListRoot = objectMapper.readTree(emptyListResponse);
            JsonNode emptyRecords = emptyListRoot.path("data").path("records");
            assertSuccessEnvelope(emptyListRoot, 1, 20);
            assertTrue(emptyRecords.isArray());
            assertEquals(0, emptyRecords.size());
            return;
        }

        MockHttpServletRequestBuilder requestBuilder = get("/api/targets/fusion")
                .param("date", sample.date())
                .param("startTime", sample.minute())
                .param("endTime", sample.minute())
                .param("pageNum", "1")
                .param("pageSize", "20");
        if (sample.targetType() != null && !sample.targetType().isBlank()) {
            requestBuilder = requestBuilder.param("targetType", sample.targetType());
        }

        String listResponse = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode listRoot = objectMapper.readTree(listResponse);
        JsonNode records = listRoot.path("data").path("records");
        assertFalse(records.isEmpty());
        JsonNode matchedRecord = findRecordByTargetId(records, sample.targetId());
        assertNotNull(matchedRecord);
        assertSuccessEnvelope(listRoot, 1, 20);
        assertFieldsPresent(matchedRecord,
                "targetId", "uavModel", "targetType", "timestampBegin", "timestampEnd", "duration", "recordCount");

        String trajectoryResponse = mockMvc.perform(get("/api/targets/fusion/{targetId}/trajectory", sample.targetId())
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0]").exists())
                .andExpect(jsonPath("$.data.records[0].targetBatch").value(Long.parseLong(sample.targetId())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode trajectoryRoot = objectMapper.readTree(trajectoryResponse);
        JsonNode trajectoryRecords = trajectoryRoot.path("data").path("records");
        assertSuccessEnvelope(trajectoryRoot, 1, 10);
        assertFieldsPresent(trajectoryRecords.get(0),
                "id", "targetBatch", "radarTargetId", "tdoaTargetId", "timestamp",
                "range", "azimuth", "pitch", "speed", "altitude", "targetLat",
                "targetLon", "targetType", "frequency", "startFrom", "duration",
                "uavModel", "whiteListId");
        assertDescending(trajectoryRecords, "timestamp");
    }

    @Test
    void shouldUseDocumentedDefaultPageValuesWhenPaginationIsOmitted() throws Exception {
        RadarSample sample = queryRadarSample();
        assertNotNull(sample);

        String listResponse = mockMvc.perform(get("/api/targets/radar")
                        .param("date", sample.date()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode listRoot = objectMapper.readTree(listResponse);
        assertSuccessEnvelope(listRoot, 1, 10);

        String trajectoryResponse = mockMvc.perform(get("/api/targets/radar/{targetId}/trajectory", sample.targetId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode trajectoryRoot = objectMapper.readTree(trajectoryResponse);
        assertSuccessEnvelope(trajectoryRoot, 1, 10);
    }

    @Test
    void shouldUseWholeDayWindowWhenOnlyDateIsProvided() throws Exception {
        TdoaSample sample = queryTdoaSample();
        assertNotNull(sample);

        String listResponse = mockMvc.perform(get("/api/targets/tdoa")
                        .param("date", sample.date())
                        .param("targetType", String.valueOf(sample.targetType())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode records = objectMapper.readTree(listResponse).path("data").path("records");
        assertNotNull(findRecordByTargetId(records, sample.targetId()));
    }

    @Test
    void shouldRejectInvalidTargetIdFormats() throws Exception {
        mockMvc.perform(get("/api/targets/radar/{targetId}/trajectory", "bad_format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request error"))
                .andExpect(jsonPath("$.data").value("targetId format must be targetBatch_targetId"));

        mockMvc.perform(get("/api/targets/radar/{targetId}/trajectory", "abc_def"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").value("targetId format must be targetBatch_targetId"));

        mockMvc.perform(get("/api/targets/tdoa/{targetId}/trajectory", "badformat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request error"))
                .andExpect(jsonPath("$.data").value("targetId format must be uavId_traceId"));

        mockMvc.perform(get("/api/targets/fusion/{targetId}/trajectory", "bad_format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request error"))
                .andExpect(jsonPath("$.data").value("targetId format must be targetBatch"));
    }

    @Test
    void shouldRejectInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get("/api/targets/radar")
                        .param("date", queryRadarSample().date())
                        .param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request error"))
                .andExpect(jsonPath("$.data").value("pageNum and pageSize must be greater than 0"));

        mockMvc.perform(get("/api/targets/fusion/{targetId}/trajectory", "1")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request error"))
                .andExpect(jsonPath("$.data").value("pageNum and pageSize must be greater than 0"));
    }

    @Test
    void shouldRejectInvalidTimeWindow() throws Exception {
        mockMvc.perform(get("/api/targets/radar")
                        .param("date", LocalDate.now().toString())
                        .param("startTime", "10:00")
                        .param("endTime", "09:59"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("request error"));
    }

    private RadarSample queryRadarSample() {
        return jdbcTemplate.queryForObject(
                """
                select concat(target_batch, '_', target_id) as target_id,
                       date_format(timestamp, '%Y-%m-%d') as query_date,
                       date_format(timestamp, '%H:%i') as query_minute,
                       target_type
                  from data_radar_target
                 where (is_delete = 0 or is_delete is null)
                   and target_type is not null
                 order by timestamp desc
                 limit 1
                """,
                (rs, rowNum) -> new RadarSample(
                        rs.getString("target_id"),
                        rs.getString("query_date"),
                        rs.getString("query_minute"),
                        rs.getInt("target_type"))
        );
    }

    private TdoaSample queryTdoaSample() {
        return jdbcTemplate.queryForObject(
                """
                select concat(uav_id, '_', trace_id) as target_id,
                       date_format(timestamp, '%Y-%m-%d') as query_date,
                       date_format(timestamp, '%H:%i') as query_minute,
                       target_type
                  from data_tdoa_target
                 where trace_id is not null
                   and target_type is not null
                 order by timestamp desc
                 limit 1
                """,
                (rs, rowNum) -> new TdoaSample(
                        rs.getString("target_id"),
                        rs.getString("query_date"),
                        rs.getString("query_minute"),
                        rs.getInt("target_type"))
        );
    }

    private FusionSample queryFusionSample() {
        List<FusionSample> samples = jdbcTemplate.query(
                """
                select cast(target_batch as char) as target_id,
                       date_format(timestamp, '%Y-%m-%d') as query_date,
                       date_format(timestamp, '%H:%i') as query_minute,
                       target_type
                 from data_fusion_target
                 order by timestamp desc
                 limit 1
                """,
                (rs, rowNum) -> new FusionSample(
                        rs.getString("target_id"),
                        rs.getString("query_date"),
                        rs.getString("query_minute"),
                        rs.getString("target_type"))
        );
        return DataAccessUtils.singleResult(samples);
    }

    private JsonNode findRecordByTargetId(JsonNode records, String targetId) {
        for (JsonNode record : records) {
            if (targetId.equals(record.path("targetId").asText())) {
                return record;
            }
        }
        return null;
    }

    private void assertSuccessEnvelope(JsonNode root, long current, long size) {
        assertEquals(200, root.path("code").asLong());
        assertEquals("succer", root.path("message").asText());
        assertEquals(current, root.path("data").path("current").asLong());
        assertEquals(size, root.path("data").path("size").asLong());
    }

    private void assertFieldsPresent(JsonNode node, String... fieldNames) {
        assertNotNull(node);
        for (String fieldName : fieldNames) {
            assertTrue(node.has(fieldName), "missing field: " + fieldName);
        }
    }

    private void assertDescending(JsonNode records, String fieldName) {
        if (records == null || !records.isArray() || records.size() < 2) {
            return;
        }

        OffsetDateTime previous = OffsetDateTime.parse(records.get(0).path(fieldName).asText());
        for (int i = 1; i < records.size(); i++) {
            OffsetDateTime current = OffsetDateTime.parse(records.get(i).path(fieldName).asText());
            assertTrue(!previous.isBefore(current), "records are not sorted in descending order");
            previous = current;
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan("com.example.coreserver.mapper")
    @Import({TargetController.class, TargetQueryService.class, MybatisPlusConfig.class})
    static class TestApplication {
    }
}
