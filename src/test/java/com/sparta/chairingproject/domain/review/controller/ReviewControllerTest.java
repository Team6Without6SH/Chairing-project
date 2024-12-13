package com.sparta.chairingproject.domain.review.controller;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@SpringBootTest
@Transactional
public class ReviewControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CommentRepository commentRepository;

	private Member testMember;
	private Store testStore;
	private Order testOrder;
	private List<Menu> menus;
	private String requestJsonMemberId;

	@BeforeEach
	void setUp() throws JsonProcessingException {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		objectMapper = new ObjectMapper();

		testMember = new Member("Test User", "user@test.com", passwordEncoder.encode("password"), MemberRole.USER);
		ReflectionTestUtils.setField(testMember, "deleted", false);

		testStore = new Store("Test Store", "Image URL", "Description", "Address", testMember);
		testStore.approveRequest();

		menus = new ArrayList<>();
		testOrder = Order.createOf(testMember, testStore, menus, OrderStatus.COMPLETED, 0);

		memberRepository.save(testMember);
		storeRepository.save(testStore);
		orderRepository.save(testOrder);

		requestJsonMemberId = objectMapper.writeValueAsString(new RequestDto(testMember.getId()));
	}

	private void setAuthentication(Member member) {
		UserDetailsImpl authMember = new UserDetailsImpl(member);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
		);
	}

	@Test
	@DisplayName("리뷰 생성 성공")
	void createReview_success() throws Exception {
		// g
		setAuthentication(testMember);
		ReviewRequest reviewRequest = new ReviewRequest("너무 좋아요♥", 5);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// w t
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isCreated())
			.andDo(print());

		Review savedReview = reviewRepository.findAll().get(0);
		assertThat(savedReview).isNotNull();
		assertThat(savedReview.getContent()).isEqualTo("너무 좋아요♥");
		assertThat(savedReview.getScore()).isEqualTo(5);
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 이미 작성된 리뷰")
	void createReview_fail_alreadyReviewed() throws Exception {
		// g
		setAuthentication(testMember);
		reviewRepository.save(Review.builder()
			.content("이미 작성된 리뷰")
			.score(5)
			.store(testStore)
			.member(testMember)
			.order(testOrder).build());

		ReviewRequest reviewRequest = new ReviewRequest("너무 좋아요♥", 5);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// w t
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.message").value(REVIEW_ALREADY_EXISTS.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 삭제된 가게")
	void createReview_fail_deletedStore() throws Exception {
		// g
		ReflectionTestUtils.setField(testStore, "deletedAt", LocalDateTime.now());
		setAuthentication(testMember);

		ReviewRequest reviewRequest = new ReviewRequest("너무 좋아요♥", 5);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// w t
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value(STORE_ALREADY_DELETED.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 팬딩 상태의 가게")
	void createReview_fail_storePending() throws Exception {
		// g
		ReflectionTestUtils.setField(testStore, "status", StoreStatus.PENDING);
		setAuthentication(testMember);

		ReviewRequest reviewRequest = new ReviewRequest("너무 좋아요♥", 5);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// w t
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value(STORE_PENDING_CANNOT_REVIEW.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 찾을 수 없는 주문")
	void createReview_fail_orderNotFound() throws Exception {
		// g
		setAuthentication(testMember);
		orderRepository.deleteAll();

		ReviewRequest reviewRequest = new ReviewRequest("너무 좋아요♥", 5);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// w t
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(NOT_FOUND_ORDER.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 완료되지 않은 주문")
	void createReview_fail_orderNotCompleted() throws Exception {
		// g
		ReflectionTestUtils.setField(testOrder, "status", OrderStatus.WAITING);
		setAuthentication(testMember);

		ReviewRequest reviewRequest = new ReviewRequest("너무 좋아요♥", 5);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// w t
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value(ORDER_NOT_COMPLETED_CANNOT_REVIEW.getMessage()))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 생성 실패 - validation 예외")
	void createCoupon_fail_emptyName() throws Exception {
		// Given
		ReviewRequest reviewRequest = new ReviewRequest("", 999);
		String requestJson = objectMapper.writeValueAsString(reviewRequest);

		// When & Then
		mockMvc.perform(post("/stores/" + testStore.getId() + "/orders/" + testOrder.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.content").value("리뷰 내용은 필수입니다."))
			.andExpect(jsonPath("$.score").value("평점은 5 이하여야 합니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 조회 성공")
	void getReviewsByStore_success() throws Exception {
		// g
		setAuthentication(testMember);

		Review review = Review.builder()
			.content("Review content")
			.store(testStore)
			.member(testMember)
			.order(testOrder)
			.build();
		reviewRepository.save(review);

		Comment comment = new Comment("Comment content", review);
		commentRepository.save(comment);

		// w t
		mockMvc.perform(get("/stores/" + testStore.getId() + "/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJsonMemberId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].review.content").value("Review content"))
			.andExpect(jsonPath("$.content[0].comment.content").value("Comment content"))
			.andDo(print());
	}

	@Test
	@DisplayName("리뷰 수정 성공")
	void updateReview_success() throws Exception {
		// Given
		setAuthentication(testMember);

		Review existingReview = Review.builder()
			.content("Original content")
			.score(4)
			.store(testStore)
			.member(testMember)
			.order(testOrder)
			.build();
		reviewRepository.save(existingReview);

		ReviewRequest updatedRequest = new ReviewRequest("Updated content", 5);
		String requestJson = objectMapper.writeValueAsString(updatedRequest);

		// When & Then
		mockMvc.perform(patch("/reviews/" + existingReview.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isOk())
			.andDo(print());

		Review updatedReview = reviewRepository.findById(existingReview.getId()).orElseThrow();
		assertThat(updatedReview.getContent()).isEqualTo("Updated content");
		assertThat(updatedReview.getScore()).isEqualTo(5);
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() throws Exception {
		// Given
		setAuthentication(testMember);

		Review reviewToDelete = Review.builder()
			.content("Review content")
			.score(5)
			.store(testStore)
			.member(testMember)
			.order(testOrder)
			.build();
		reviewRepository.save(reviewToDelete);

		String requestJson = objectMapper.writeValueAsString(new RequestDto(testMember.getId()));

		// When & Then
		mockMvc.perform(delete("/reviews/" + reviewToDelete.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestJson))
			.andExpect(status().isOk())
			.andDo(print());

		Review deletedReview = reviewRepository.findById(reviewToDelete.getId()).orElseThrow();
		assertThat(deletedReview.getDeletedAt()).isNotNull();
	}
}
