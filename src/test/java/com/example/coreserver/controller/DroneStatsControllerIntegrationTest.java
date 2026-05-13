package com.example.coreserver.controller;

import com.example.coreserver.config.MybatisPlusConfig;
import com.example.coreserver.service.business.TargetMonitorStatsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = DroneStatsControllerIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc(addFilters = false)
class DroneStatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnDailyStatsFromTargetMonitorStat() throws Exception {
        LocalDate today = LocalDate.now();
        Map<String, Object> expected = queryDailyStat(today);

        String response = mockMvc.perform(get("/api/drone-stats/daily"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root.path("data");
        assertSuccess(root);
        assertEquals(today.toString(), data.path("statTime").asText());
        assertEquals(asInt(expected.get("fusion_target_count")), data.path("fusionTargetCount").asInt());
        assertEquals(asInt(expected.get("fusion_illegal_count")), data.path("fusionIllegalCount").asInt());
        assertEquals(asInt(expected.get("effective_dispose_count")), data.path("effectiveDisposeCount").asInt());
    }

    @Test
    void shouldReturnDailyStatsForSpecifiedDate() throws Exception {
        LocalDate statDate = queryLatestStatDate();
        Map<String, Object> expected = queryDailyStat(statDate);

        String response = mockMvc.perform(get("/api/drone-stats/daily")
                        .param("date", statDate.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root.path("data");
        assertSuccess(root);
        assertEquals(statDate.toString(), data.path("statTime").asText());
        assertEquals(asInt(expected.get("fusion_target_count")), data.path("fusionTargetCount").asInt());
        assertEquals(asInt(expected.get("fusion_illegal_count")), data.path("fusionIllegalCount").asInt());
        assertEquals(asInt(expected.get("effective_dispose_count")), data.path("effectiveDisposeCount").asInt());
    }

    @Test
    void shouldReturnSevenDayTrendAndPadMissingDaysWithZero() throws Exception {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        Map<String, Map<String, Object>> expected = queryTrendStats(startDate, endDate);

        String response = mockMvc.perform(get("/api/drone-stats/trend"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root.path("data");
        assertSuccess(root);
        assertTrue(data.isArray());
        assertEquals(7, data.size());

        for (int i = 0; i < data.size(); i++) {
            JsonNode point = data.get(i);
            LocalDate expectedDate = startDate.plusDays(i);
            assertEquals(expectedDate.toString(), point.path("statTime").asText());

            Map<String, Object> expectedRow = expected.get(expectedDate.toString());
            if (expectedRow == null) {
                assertEquals(0, point.path("fusionTargetCount").asInt());
                assertEquals(0, point.path("fusionIllegalCount").asInt());
                assertEquals(0, point.path("effectiveDisposeCount").asInt());
            } else {
                assertEquals(asInt(expectedRow.get("fusion_target_count")), point.path("fusionTargetCount").asInt());
                assertEquals(asInt(expectedRow.get("fusion_illegal_count")), point.path("fusionIllegalCount").asInt());
                assertEquals(asInt(expectedRow.get("effective_dispose_count")), point.path("effectiveDisposeCount").asInt());
            }
        }
    }

    @Test
    void shouldReturnSevenDayTrendForSpecifiedEndDate() throws Exception {
        LocalDate endDate = queryLatestStatDate();
        LocalDate startDate = endDate.minusDays(6);
        Map<String, Map<String, Object>> expected = queryTrendStats(startDate, endDate);

        String response = mockMvc.perform(get("/api/drone-stats/trend")
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root.path("data");
        assertSuccess(root);
        assertEquals(7, data.size());
        assertEquals(startDate.toString(), data.get(0).path("statTime").asText());
        assertEquals(endDate.toString(), data.get(6).path("statTime").asText());

        for (JsonNode point : data) {
            Map<String, Object> expectedRow = expected.get(point.path("statTime").asText());
            if (expectedRow == null) {
                assertEquals(0, point.path("fusionTargetCount").asInt());
                assertEquals(0, point.path("fusionIllegalCount").asInt());
                assertEquals(0, point.path("effectiveDisposeCount").asInt());
            }
        }
    }

    private Map<String, Object> queryDailyStat(LocalDate statDate) {
        Map<String, Object> result = jdbcTemplate.query(
                """
                select fusion_target_count, fusion_illegal_count, effective_dispose_count
                  from target_monitor_stat
                 where stat_time = ?
                """,
                ps -> ps.setObject(1, statDate),
                rs -> {
                    if (!rs.next()) {
                        Map<String, Object> empty = new HashMap<>();
                        empty.put("fusion_target_count", 0);
                        empty.put("fusion_illegal_count", 0);
                        empty.put("effective_dispose_count", 0);
                        return empty;
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("fusion_target_count", rs.getInt("fusion_target_count"));
                    row.put("fusion_illegal_count", rs.getInt("fusion_illegal_count"));
                    row.put("effective_dispose_count", rs.getInt("effective_dispose_count"));
                    return row;
                }
        );
        assertNotNull(result);
        return result;
    }

    private Map<String, Map<String, Object>> queryTrendStats(LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(
                """
                select stat_time, fusion_target_count, fusion_illegal_count, effective_dispose_count
                  from target_monitor_stat
                 where stat_time between ? and ?
                 order by stat_time asc
                """,
                ps -> {
                    ps.setObject(1, startDate);
                    ps.setObject(2, endDate);
                },
                rs -> {
                    Map<String, Map<String, Object>> rows = new HashMap<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("fusion_target_count", rs.getInt("fusion_target_count"));
                        row.put("fusion_illegal_count", rs.getInt("fusion_illegal_count"));
                        row.put("effective_dispose_count", rs.getInt("effective_dispose_count"));
                        rows.put(rs.getDate("stat_time").toLocalDate().toString(), row);
                    }
                    return rows;
                }
        );
    }

    private LocalDate queryLatestStatDate() {
        return jdbcTemplate.queryForObject(
                "select max(stat_time) from target_monitor_stat",
                LocalDate.class
        );
    }

    private int asInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private void assertSuccess(JsonNode root) {
        assertEquals(200, root.path("code").asInt());
        assertEquals("succer", root.path("message").asText());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.example.coreserver.entity")
    @EnableJpaRepositories("com.example.coreserver.repository")
    @MapperScan("com.example.coreserver.mapper")
    @Import({DroneStatsController.class, TargetMonitorStatsService.class, MybatisPlusConfig.class})
    static class TestApplication {
    }
}
