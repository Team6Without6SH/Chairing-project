package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    MemberRepository memberRepository;

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
        memberRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("권한 검증 - ADMIN 권한으로 쿠폰 생성 성공")
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
    @DisplayName("권한 검증 - USER 권한으로 쿠폰 생성 금지")
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
    @DisplayName("쿠폰수량, 할인 가격은 0 이상")
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

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("권한 검증 - ADMIN 권한으로 쿠폰 발급 금지")
    void issueCoupon_forbidden_withAdminRole() throws Exception {
        // Given
        Long couponId = 1L;
        String requestBody = """
                {
                    "couponId": %d
                }
                """.formatted(couponId);

        // When & Then
        mockMvc.perform(post("/coupons/" + couponId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()) // HTTP 403 Forbidden
                .andDo(print());
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER 권한으로 쿠폰 전체 조회 실패")
    void getAllCoupons_forbidden_withUserRole() throws Exception {
        // Given
        int page = 1;
        int size = 10;
        String requestBody = """
                {
                    "memberId": 1
                }
                """;

        // When & Then
        mockMvc.perform(get("/coupons")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)) // 요청 본문 추가
                .andExpect(status().isForbidden()) // HTTP 403 Forbidden
                .andDo(print());
    }
}
