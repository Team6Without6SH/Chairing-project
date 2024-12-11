package com.sparta.chairingproject.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@SpringBootTest
@Transactional
public class MemberControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private IssuanceRepository issuanceRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	private Member testMember;
	private Coupon coupon;
	private Issuance issuance;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		objectMapper = new ObjectMapper();

		testMember = new Member("testName", "test@test.com",
			passwordEncoder.encode("encodedPassword"), MemberRole.USER);
		ReflectionTestUtils.setField(testMember, "deleted", false);

		coupon = Coupon.builder()
			.name("test coupon")
			.quantity(100)
			.discountPrice(5000)
			.build();

		issuance = Issuance.builder()
			.member(testMember)
			.coupon(coupon)
			.build();

		memberRepository.save(testMember);
		couponRepository.save(coupon);
		issuanceRepository.save(issuance);

	}

	@Test
	@DisplayName("내 정보 조회")
	void getMemberDetails() throws Exception {

		setAuthentication(testMember);

		mockMvc.perform(get("/members").principal(() -> testMember.getEmail()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value(testMember.getEmail()))
			.andExpect(jsonPath("$.name").value(testMember.getName()));

		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("비밀번호 변경")
	void updatePassword() throws Exception {

		setAuthentication(testMember);

		MemberPasswordRequest passwordRequest = new MemberPasswordRequest("encodedPassword",
			"newPassword",
			"newPassword");

		mockMvc.perform(patch("/members")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(passwordRequest)))
			.andExpect(status().isOk());

		Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
		assertThat(passwordEncoder.matches("newPassword", updatedMember.getPassword())).isTrue();
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("내 주문 조회")
	void getMemberOrders() throws Exception {
		setAuthentication(testMember);

		RequestDto requestDto = new RequestDto();

		mockMvc.perform(get("/members/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray());

		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("내 예약 조회")
	void getMemberReservations() throws Exception {
		setAuthentication(testMember);

		RequestDto requestDto = new RequestDto();

		mockMvc.perform(get("/members/reservations")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(0)); // 데이터가 없을 경우

		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("내 쿠폰 조회")
	void getMemberIssuance() throws Exception {
		setAuthentication(testMember);

		RequestDto requestDto = new RequestDto();

		mockMvc.perform(get("/members/coupons")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(1)); // 데이터가 없을 경우

		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("회원 탈퇴")
	void deleteMember() throws Exception {
		setAuthentication(testMember);

		RequestDto requestDto = new RequestDto();

		mockMvc.perform(delete("/members/delete")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk());

		Member deletedMember = memberRepository.findById(testMember.getId()).orElseThrow();
		assertThat(deletedMember.isDeleted()).isTrue();

		SecurityContextHolder.clearContext();
	}

	private void setAuthentication(Member member) {
		UserDetailsImpl authMember = new UserDetailsImpl(testMember);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
		);
	}
}
