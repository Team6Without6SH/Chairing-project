package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // 테스트 환경에서 ADMIN과 USER 권한의 가짜 토큰을 준비 (실제 환경에서는 JWT 등을 통해 토큰 생성)
        adminToken = "Bearer admin-test-token";
        userToken = "Bearer user-test-token";
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCoupon_success_withAdminRole() throws Exception {
        // Given
        String requestBody = """
                {
                    "name": "Spring Sale",
                    "quantity": 100,
                    "discountPrice": 5000
                }
                """;

        // When & Then
        mockMvc.perform(post("/coupons")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()) // HTTP 201 Created
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Spring Sale"))
                .andExpect(jsonPath("$.quantity").value(100))
                .andExpect(jsonPath("$.discountPrice").value(5000))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createCoupon_forbidden_withUserRole() throws Exception {
        // Given
        String requestBody = """
                {
                    "name": "Spring Sale",
                    "quantity": 100,
                    "discountPrice": 5000
                }
                """;

        // When & Then
        mockMvc.perform(post("/coupons")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden()) // HTTP 403 Forbidden
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCoupon_validationError_whenRequestBodyIsInvalid() throws Exception {
        // Given
        String invalidRequestBody = """
                {
                    "name": "",
                    "quantity": -1,
                    "discountPrice": -5000
                }
                """;

        // When & Then
        mockMvc.perform(post("/coupons")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest()) // HTTP 400 Bad Request
                .andDo(print());
    }
}