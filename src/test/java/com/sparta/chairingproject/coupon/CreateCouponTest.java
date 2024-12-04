//package com.sparta.chairingproject.coupon;
//
//import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//class CouponControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private CouponRepository couponRepository;
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void createCoupon_success() throws Exception {
//        // Given
//        String requestBody = """
//                {
//                    "name": "Spring Sale",
//                    "quantity": 100,
//                    "discountPrice": 5000
//                }
//                """;
//
//        // When & Then
//        mockMvc.perform(post("/api/admin/coupons")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.statusCode").value("Success"))
//                .andExpect(jsonPath("$.message").value("쿠폰 생성 성공"))
//                .andDo(print());
//
//        // Repository 확인
//        assertTrue(couponRepository.findAll().size() > 0);
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    void createCoupon_forbidden() throws Exception {
//        // Given
//        String requestBody = """
//                {
//                    "name": "Spring Sale",
//                    "quantity": 100,
//                    "discountPrice": 5000
//                }
//                """;
//
//        // When & Then
//        mockMvc.perform(post("/api/admin/coupons")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.statusCode").value("Error"))
//                .andExpect(jsonPath("$.message").value("쿠폰 생성 권한이 없습니다."))
//                .andDo(print());
//    }
//}
//


//import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
//import com.sparta.chairingproject.domain.coupon.entity.Coupon;
//import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
//import com.sparta.chairingproject.domain.member.entity.MemberRole;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@RequiredArgsConstructor
//class CouponControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private CouponRepository couponRepository;
//
//    @Autowired
//    private IssuanceRepository issuanceRepository;
//
//    @Test
//    @WithMockUser(roles = "USER")
//    void issueCoupon_success() throws Exception {
//        // Given
//        Coupon coupon = couponRepository.save(new Coupon("Spring Sale", 100, 5000));
//        Long couponId = coupon.getId();
//
//        // When & Then
//        mockMvc.perform(post("/api/coupons/" + couponId + "/issue"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.statusCode").value("Success"))
//                .andExpect(jsonPath("$.message").value("쿠폰이 성공적으로 발급되었습니다."))
//                .andDo(print());
//
//        // Repository 확인
//        assertTrue(issuanceRepository.findAll().size() > 0);
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    void issueCoupon_alreadyIssued() throws Exception {
//        // Given
//        Coupon coupon = couponRepository.save(new Coupon("Spring Sale", 100, 5000));
//        Long couponId = coupon.getId();
//        issuanceRepository.save(new Issuance(mockMember(), coupon));
//
//        // When & Then
//        mockMvc.perform(post("/api/coupons/" + couponId + "/issue"))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.statusCode").value("Error"))
//                .andExpect(jsonPath("$.message").value("이미 해당 쿠폰을 발급받았습니다."))
//                .andDo(print());
//    }
//
//    @Test
//    @WithMockUser(roles = "USER")
//    void issueCoupon_noQuantity() throws Exception {
//        // Given
//        Coupon coupon = couponRepository.save(new Coupon("Spring Sale", 0, 5000)); // 수량 0
//        Long couponId = coupon.getId();
//
//        // When & Then
//        mockMvc.perform(post("/api/coupons/" + couponId + "/issue"))
//                .andExpect(status().isConflict())
//                .andExpect(jsonPath("$.statusCode").value("Error"))
//                .andExpect(jsonPath("$.message").value("쿠폰 수량이 부족합니다."))
//                .andDo(print());
//    }
//
//    private Member mockMember() {
//        return new Member("user", "password", MemberRole.USER);
//    }
//}
