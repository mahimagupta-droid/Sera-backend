package com.era.emergency.controller;

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
class HospitalControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test @Order(1)
    void shouldRegisterHospitalSuccessfully() throws Exception {
        var payload = Map.of(
                "name",                   "Test General Hospital",
                "address",                "123 Test Street",
                "city",                   "New Delhi",
                "latitude",               28.60,
                "longitude",              77.20,
                "totalIcuBeds",           20,
                "availableIcuBeds",       10,
                "totalGeneralBeds",       100,
                "availableGeneralBeds",   50,
                "availableAmbulances",    4,
                "isActive",               true,
                "traumaCenter",           false
        );

        mockMvc.perform(post("/hospital")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test General Hospital"))
                .andExpect(jsonPath("$.data.capacityScore").isNumber());
    }

    @Test @Order(2)
    void shouldFetchAllHospitals() throws Exception {
        mockMvc.perform(get("/hospital"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(3)
    void shouldFetchActiveHospitals() throws Exception {
        mockMvc.perform(get("/hospital/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test @Order(4)
    void shouldReturn404ForUnknownHospital() throws Exception {
        mockMvc.perform(get("/hospital/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test @Order(5)
    void shouldRejectHospitalWithMissingName() throws Exception {
        var payload = Map.of(
                "address",   "No Name Street",
                "city",      "Delhi",
                "latitude",  28.60,
                "longitude", 77.20
        );

        mockMvc.perform(post("/hospital")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
