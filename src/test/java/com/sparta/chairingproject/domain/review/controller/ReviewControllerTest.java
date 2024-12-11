// package com.sparta.chairingproject.domain.review.controller;
//
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;
//
// import com.sparta.chairingproject.config.security.UserDetailsImpl;
// import com.sparta.chairingproject.domain.member.entity.Member;
// import com.sparta.chairingproject.domain.member.entity.MemberRole;
//
// @SpringBootTest
// @AutoConfigureMockMvc
// class ReviewControllerTest {
//
// 	@Autowired
// 	private MockMvc mockMvc;
//
// 	@Test
// 	@WithMockUser(username = "user", roles = {"USER"})
// 	@DisplayName("권한 검증 - USER 권한으로 리뷰 작성 성공")
// 	void createReview_success_withUserRole() throws Exception {
// 		// Given
// 		Member mockMember = new Member(1L, "user", "user@gmail.com", "1234", MemberRole.USER);
//
// 		UserDetailsImpl mockUserDetails = new UserDetailsImpl(mockMember);
// 		SecurityContextHolder.getContext().setAuthentication(
// 			new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())
// 		);
//
// 		String requestBody = """
// 			{
// 			    "memberId": 1,
// 			    "content": "좋은 가게였습니다.",
// 			    "score": 5
// 			}
// 			""";
//
// 		// When & Then
// 		mockMvc.perform(post("/stores/1/reviews")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestBody))
// 			.andExpect(status().isCreated()) // HTTP 201 Created
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@WithMockUser(username = "admin", roles = {"ADMIN"})
// 	@DisplayName("권한 검증 - ADMIN 권한으로 리뷰 작성 실패")
// 	void createReview_forbidden_withAdminRole() throws Exception {
// 		// Given
// 		String requestBody = """
// 			{
// 			    "memberId": 1,
// 			    "content": "좋은 가게였습니다.",
// 			    "score": 5
// 			}
// 			""";
//
// 		// When & Then
// 		mockMvc.perform(post("/stores/1/reviews")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestBody))
// 			.andExpect(status().isForbidden()) // HTTP 403 Forbidden
// 			.andDo(print());
// 	}
//
// }
