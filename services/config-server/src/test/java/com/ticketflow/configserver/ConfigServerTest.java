package com.ticketflow.configserver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:/test-config/"})
@AutoConfigureMockMvc
class ConfigServerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void servesTheConfigurationOfAService() throws Exception {
        mockMvc.perform(get("/customer-service/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[0].source['ticketflow.message']")
                        .value("from test"));
    }
}