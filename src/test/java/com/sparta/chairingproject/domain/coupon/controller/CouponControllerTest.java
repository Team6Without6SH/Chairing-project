package com.sparta.chairingproject.domain.coupon.controller;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CouponControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Member testMember;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		objectMapper = new ObjectMapper();

		testMember = new Member("testName", "test@test.com",
			passwordEncoder.encode("encodedPassword"), MemberRole.USER);
		ReflectionTestUtils.setField(testMember, "deleted", false);

		memberRepository.save(testMember);
	}

	private void setAuthentication(Member member) {
		UserDetailsImpl authMember = new UserDetailsImpl(testMember);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
		);
	}

	@Test
	@DisplayName("쿠폰 생성 성공")
	void createAndGetCoupon() throws Exception {

		// Given
		ReflectionTestUtils.setField(testMember, "memberRole", MemberRole.ADMIN);
		setAuthentication(testMember);
		// 쿠폰 생성 요청
		CouponRequest couponRequest = new CouponRequest("test coupon", 100, 5000);
		String requestJson = objectMapper.writeValueAsString(couponRequest);

		// POST 요청으로 쿠폰 생성
		mockMvc.perform(post("/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("test coupon"))
			.andExpect(jsonPath("$.quantity").value(100))
			.andExpect(jsonPath("$.discountPrice").value(5000));

		// DB에 쿠폰이 저장되었는지 확인
		Coupon savedCoupon = couponRepository.findAll().get(0);
		assertThat(savedCoupon).isNotNull();
		assertThat(savedCoupon.getName()).isEqualTo("test coupon");
	}

	@Test
	@DisplayName("쿠폰 생성 실패 - 중복된 이름")
	void createCoupon_fail_duplicateName() throws Exception {
		// Given
		ReflectionTestUtils.setField(testMember, "memberRole", MemberRole.ADMIN);
		setAuthentication(testMember);

		// 이미 존재하는 쿠폰 저장
		Coupon existingCoupon = Coupon.builder()
			.name("Duplicate Coupon")
			.quantity(50)
			.discountPrice(1000)
			.build();
		couponRepository.save(existingCoupon);

		// 동일한 이름으로 생성 요청
		CouponRequest request = new CouponRequest("Duplicate Coupon", 10, 500);
		String requestJson = objectMapper.writeValueAsString(request);

		// When & Then
		mockMvc.perform(post("/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(COUPON_NAME_ALREADY_EXISTS.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("쿠폰 조회 성공")
	void getAllCoupons_success() throws Exception {
		// Given
		ReflectionTestUtils.setField(testMember, "memberRole", MemberRole.ADMIN);
		setAuthentication(testMember);

		Coupon coupon1 = Coupon.builder()
			.name("Coupon A")
			.quantity(50)
			.discountPrice(1000)
			.build();

		Coupon coupon2 = Coupon.builder()
			.name("Coupon B")
			.quantity(30)
			.discountPrice(500)
			.build();

		couponRepository.save(coupon1);
		couponRepository.save(coupon2);

		String requestBody = objectMapper.writeValueAsString(new RequestDto(testMember.getId()));

		// When & Then
		mockMvc.perform(get("/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody)
				.param("page", "1")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name").value("Coupon A"))
			.andExpect(jsonPath("$.content[1].name").value("Coupon B"))
			.andDo(print());
	}

}
