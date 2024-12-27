// package com.sparta.chairingproject.domain.comment.controller;
//
// import static org.assertj.core.api.AssertionsForClassTypes.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.util.ArrayList;
// import java.util.List;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.context.WebApplicationContext;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sparta.chairingproject.config.security.UserDetailsImpl;
// import com.sparta.chairingproject.domain.comment.dto.CommentRequest;
// import com.sparta.chairingproject.domain.comment.entity.Comment;
// import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
// import com.sparta.chairingproject.domain.common.dto.RequestDto;
// import com.sparta.chairingproject.domain.member.entity.Member;
// import com.sparta.chairingproject.domain.member.entity.MemberRole;
// import com.sparta.chairingproject.domain.member.repository.MemberRepository;
// import com.sparta.chairingproject.domain.menu.entity.Menu;
// import com.sparta.chairingproject.domain.order.entity.Order;
// import com.sparta.chairingproject.domain.order.entity.OrderStatus;
// import com.sparta.chairingproject.domain.order.repository.OrderRepository;
// import com.sparta.chairingproject.domain.review.entity.Review;
// import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
// import com.sparta.chairingproject.domain.store.entity.Store;
// import com.sparta.chairingproject.domain.store.repository.StoreRepository;
//
// @SpringBootTest
// @Transactional
// public class CommentControllerTest {
//
// 	private MockMvc mockMvc;
//
// 	private ObjectMapper objectMapper;
//
// 	@Autowired
// 	private MemberRepository memberRepository;
//
// 	@Autowired
// 	private StoreRepository storeRepository;
//
// 	@Autowired
// 	private OrderRepository orderRepository;
//
// 	@Autowired
// 	private ReviewRepository reviewRepository;
//
// 	@Autowired
// 	private WebApplicationContext context;
//
// 	@Autowired
// 	private PasswordEncoder passwordEncoder;
//
// 	@Autowired
// 	private CommentRepository commentRepository;
//
// 	private Member testMember;
// 	private Store testStore;
// 	private Order testOrder;
// 	private Review testReview;
// 	private List<Menu> menus;
//
// 	@BeforeEach
// 	void setUp() {
// 		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
// 		objectMapper = new ObjectMapper();
//
// 		testMember = new Member("Test User", "user@test.com", passwordEncoder.encode("password"), MemberRole.OWNER);
// 		ReflectionTestUtils.setField(testMember, "deleted", false);
//
// 		testStore = new Store("Test Store", "Image URL", "Description", "Address", testMember);
// 		testStore.approveRequest();
//
// 		menus = new ArrayList<>();
// 		testOrder = Order.createOf(testMember, testStore, menus, OrderStatus.COMPLETED, 10000);
//
// 		testReview = Review.builder()
// 			.content("Review content")
// 			.score(5)
// 			.store(testStore)
// 			.member(testMember)
// 			.order(testOrder)
// 			.build();
//
// 		memberRepository.save(testMember);
// 		storeRepository.save(testStore);
// 		orderRepository.save(testOrder);
// 		reviewRepository.save(testReview);
// 	}
//
// 	private void setAuthentication(Member member) {
// 		UserDetailsImpl authMember = new UserDetailsImpl(member);
// 		SecurityContextHolder.getContext().setAuthentication(
// 			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
// 		);
// 	}
//
// 	@Test
// 	@DisplayName("댓글 작성 성공")
// 	void createComment_success() throws Exception {
// 		// g
// 		setAuthentication(testMember);
//
// 		CommentRequest commentRequest = new CommentRequest("This is a test comment.");
// 		String requestJson = objectMapper.writeValueAsString(commentRequest);
//
// 		// w t
// 		mockMvc.perform(post("/owners/stores/" + testStore.getId() + "/reviews/" + testReview.getId() + "/comments")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson))
// 			.andExpect(status().isCreated())
// 			.andDo(print());
//
// 		Comment savedComment = commentRepository.findAll().get(0);
// 		assertThat(savedComment).isNotNull();
// 		assertThat(savedComment.getContent()).isEqualTo("This is a test comment.");
// 		assertThat(savedComment.getReview().getId()).isEqualTo(testReview.getId());
// 	}
//
// 	@Test
// 	@DisplayName("댓글 수정 성공")
// 	void updateComment_success() throws Exception {
// 		// G
// 		setAuthentication(testMember);
//
// 		Comment comment = new Comment("comment content", testReview);
// 		commentRepository.save(comment);
//
// 		CommentRequest updatedRequest = new CommentRequest("Updated comment content");
// 		String requestJson = objectMapper.writeValueAsString(updatedRequest);
//
// 		// w t
// 		mockMvc.perform(patch("/reviews/" + testReview.getId() + "/comments/" + comment.getId())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson))
// 			.andExpect(status().isOk())
// 			.andDo(print());
//
// 		Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
// 		assertThat(updatedComment.getContent()).isEqualTo("Updated comment content");
// 	}
//
// 	@Test
// 	@DisplayName("댓글 삭제 성공")
// 	void deleteComment_success() throws Exception {
// 		// Given
// 		setAuthentication(testMember);
//
// 		Comment comment = new Comment("comment content", testReview);
// 		commentRepository.save(comment);
//
// 		String requestJson = objectMapper.writeValueAsString(new RequestDto(testMember.getId()));
//
// 		// When & Then
// 		mockMvc.perform(delete("/reviews/" + testReview.getId() + "/comments/" + comment.getId())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson))
// 			.andExpect(status().isOk())
// 			.andDo(print());
//
// 		Comment deletedComment = commentRepository.findById(comment.getId()).orElseThrow();
// 		assertThat(deletedComment.getDeletedAt()).isNotNull();
// 	}
// }
