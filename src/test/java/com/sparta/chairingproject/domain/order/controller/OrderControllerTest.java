package com.sparta.chairingproject.domain.order.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@TestPropertySource(locations = "/application-test.properties")
@SpringBootTest
@Transactional
class OrderControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext context;

	private ObjectMapper objectMapper;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private MenuRepository menuRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private OrderRepository orderRepository;

	private Member testMember;
	private Member testOwner;
	private Store store;
	private Menu menu1;
	private Menu menu2;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		objectMapper = new ObjectMapper();

		testMember = new Member("T member", "test@email.com", "password1234", MemberRole.USER);
		testOwner = new Member("T Owner", "testOwner@email.com", "password1234", MemberRole.OWNER);
		store = new Store("T Store", "testImage.jpg", "Test store death", "seoul", testOwner);
		ReflectionTestUtils.setField(store, "tableCount", 5);
		menu1 = Menu.createOf("menu1", 10000, "menuImage.jpg", store);
		menu2 = Menu.createOf("menu2", 20000, "menuImage2.jpg", store);

		memberRepository.save(testMember);
		memberRepository.save(testOwner);
		storeRepository.save(store);
		menuRepository.save(menu1);
		menuRepository.save(menu2);
	}

	@Test
	@DisplayName("정상 주문 생성 - 여유 테이블 있을 때")
	void createOrder_success() throws Exception {

		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), menu2.getId()), 30000);
		setAuthentication(testMember);

		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.orderStatus").value("ADMISSION"))
			.andExpect(jsonPath("$.totalPrice").value(30000))
			.andExpect(jsonPath("$.menuNames", hasSize(2)))
			.andExpect(jsonPath("$.menuNames[0]").value("menu1"))
			.andExpect(jsonPath("$.menuNames[1]").value("menu2"));
	}

	@Test
	@DisplayName("정상 주문 생성 - 메뉴 없이 요청")
	void createOrder_NoMenus() throws Exception {

		OrderRequest orderRequest = new OrderRequest(List.of(), 0);
		setAuthentication(testMember);

		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.orderStatus").value("ADMISSION")) //현재 따로 IN_PROGRESS 인 사람이 없기 때문
			.andExpect(jsonPath("$.totalPrice").value(0))
			.andExpect(jsonPath("$.menuNames").isEmpty());
	}

	@Test
	@DisplayName("존재하지 않는 가게로 주문 요청")
	void createOrder_NotFoundStore() throws Exception {
		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId()), 10000);
		setAuthentication(testMember);

		mockMvc.perform(post("/stores/9999/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("NOT_FOUND_STORE"));
	}

	@Test
	@DisplayName("잘못된 메뉴 포함된 주문 요청")
	void createOrder_NotFoundMenu() throws Exception {
		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), 999L), 3000);
		setAuthentication(testMember);

		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("NOT_ORDER_THIS_STORE"));
	}

	@Test
	@DisplayName("결제 금액 불일치")
	void createOrder_PaymentNotMatched() throws Exception {
		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), menu2.getId()), 4000);
		setAuthentication(testMember);

		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("PAYED_NOT_EQUAL_BILL"));
	}

	@Test
	@DisplayName("여유 테이블이 없는 경우는 WAITING 상태로 생성")
	void createOrder_Waiting() throws Exception {
		for (int i = 0; i < 5; i++) {
			orderRepository.save(Order.createOf(testMember, store, List.of(), OrderStatus.IN_PROGRESS, 0));
		}
		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), menu2.getId()), 30000);
		setAuthentication(testMember);

		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
				.principal(() -> testMember.getEmail())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(orderRequest)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.orderStatus").value("WAITING"))
			.andExpect(jsonPath("$.totalPrice").value(30000));

	}

	private void setAuthentication(Member member) {
		UserDetailsImpl authMember = new UserDetailsImpl(testMember);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
		);
	}
}