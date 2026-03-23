package com.era.emergency.controller;

import com.era.emergency.model.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmergencyControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ─── POST /emergency ──────────────────────────────────────────────────────

    @Test @Order(1)
    void shouldSubmitEmergencySuccessfully() throws Exception {
        var payload = Map.of(
                "patientName",    "Integration Test Patient",
                "patientAge",     40,
                "severity",       "HIGH",
                "emergencyType",  "Road Accident",
                "description",    "Integration test emergency",
                "latitude",       28.55,
                "longitude",      77.25,
                "locationAddress","Test Location, Delhi"
        );

        mockMvc.perform(post("/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.patientName").value("Integration Test Patient"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test @Order(2)
    void shouldRejectEmergencyWithMissingFields() throws Exception {
        var payload = Map.of("patientName", "Incomplete");   // missing required fields

        mockMvc.perform(post("/emergency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── GET /emergency ───────────────────────────────────────────────────────

    @Test @Order(3)
    void shouldFetchAllEmergencies() throws Exception {
        mockMvc.perform(get("/emergency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(4)
    void shouldFetchPendingEmergencies() throws Exception {
        mockMvc.perform(get("/emergency/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ─── GET /allocate ────────────────────────────────────────────────────────

    @Test @Order(5)
    void shouldAllocateAllPendingEmergencies() throws Exception {
        mockMvc.perform(get("/allocate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ─── GET /emergency/{id} ──────────────────────────────────────────────────

    @Test @Order(6)
    void shouldReturn404ForNonExistentEmergency() throws Exception {
        mockMvc.perform(get("/emergency/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
