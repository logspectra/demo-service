package com.example.gateway.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloReturnsMessage() throws Exception {
        mockMvc.perform(get("/api/hello").param("name", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, test"));
    }

    @Test
    void errorBurstReturnsEmittedCount() throws Exception {
        mockMvc.perform(get("/api/error-burst").param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emitted").value(5))
                .andExpect(jsonPath("$.level").value("ERROR"));
    }
}
