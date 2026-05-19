package co.uk.next.loyalty.admin.api.controller;

import co.uk.next.loyalty.admin.domain.model.RatesConfig;
import co.uk.next.loyalty.admin.infrastructure.persistence.RatesConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatesConfigRepository ratesConfigRepository;

    @BeforeEach
    void setUp() {
        ratesConfigRepository.deleteAll();
        RatesConfig config = new RatesConfig();
        config.setAccrualRate(1.0);
        config.setRedemptionRate(20.0);
        config.setMinimumRedemption(100);
        config.setMaxDiscountPercentage(50.0);
        config.setExpirationMonths(12);
        config.setUpdatedAt(Instant.now());
        ratesConfigRepository.save(config);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboardReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCampaigns").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRatesReturns200() throws Exception {
        mockMvc.perform(put("/api/v1/admin/config/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accrualRate": 1.5, "redemptionRate": 25.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accrualRate").value(1.5))
                .andExpect(jsonPath("$.redemptionRate").value(25.0));
    }

    @Test
    void unauthorizedReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
