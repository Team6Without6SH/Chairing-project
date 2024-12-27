// package com.sparta.chairingproject.domain.order.controller;
//
// import static org.hamcrest.Matchers.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.test.util.ReflectionTestUtils;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.context.WebApplicationContext;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.sparta.chairingproject.config.security.UserDetailsImpl;
// import com.sparta.chairingproject.domain.common.dto.RequestDto;
// import com.sparta.chairingproject.domain.member.entity.Member;
// import com.sparta.chairingproject.domain.member.entity.MemberRole;
// import com.sparta.chairingproject.domain.member.repository.MemberRepository;
// import com.sparta.chairingproject.domain.menu.entity.Menu;
// import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
// import com.sparta.chairingproject.domain.order.dto.request.OrderCancelRequest;
// import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
// import com.sparta.chairingproject.domain.order.dto.request.OrderStatusUpdateRequest;
// import com.sparta.chairingproject.domain.order.entity.Order;
// import com.sparta.chairingproject.domain.order.entity.OrderStatus;
// import com.sparta.chairingproject.domain.order.repository.OrderRepository;
// import com.sparta.chairingproject.domain.store.entity.Store;
// import com.sparta.chairingproject.domain.store.repository.StoreRepository;
//
// @TestPropertySource(locations = "/application-test.properties")
// @SpringBootTest
// @Transactional
// class OrderControllerTest {
//
// 	private MockMvc mockMvc;
//
// 	@Autowired
// 	private WebApplicationContext context;
//
// 	private ObjectMapper objectMapper;
//
// 	@Autowired
// 	private StoreRepository storeRepository;
//
// 	@Autowired
// 	private MenuRepository menuRepository;
//
// 	@Autowired
// 	private MemberRepository memberRepository;
//
// 	@Autowired
// 	private OrderRepository orderRepository;
//
// 	private Member testMember;
// 	private Member anotherMember;
// 	private Member testOwner;
// 	private Member anotherOwner;
// 	private Store store;
// 	private Store anotherStore;
// 	private Menu menu1;
// 	private Menu menu2;
// 	private Order order;
// 	private Order cancelledOrder;
// 	private Order anotherOrder;
// 	private List<Menu> menuList;
//
// 	@BeforeEach
// 	void setUp() {
// 		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
// 		objectMapper = new ObjectMapper();
//
// 		testMember = new Member("T member", "test@email.com", "password1234", MemberRole.USER);
// 		anotherMember = new Member("T member2", "test2@email.com", "password1234", MemberRole.USER);
// 		testOwner = new Member("T Owner", "testOwner@email.com", "password1234", MemberRole.OWNER);
// 		anotherOwner = new Member("T Owner2", "testOwner2@email.com", "password1234", MemberRole.OWNER);
// 		store = new Store("T Store", "testImage.jpg", "Test store death", "seoul", testOwner);
// 		anotherStore = new Store("T Store2", "testImage2.jpg", "Test store death2", "seoul", testOwner);
// 		ReflectionTestUtils.setField(store, "tableCount", 5);
// 		ReflectionTestUtils.setField(anotherStore, "tableCount", 5);
// 		menu1 = Menu.createOf("menu1", 10000, "menuImage.jpg", store);
// 		menu2 = Menu.createOf("menu2", 20000, "menuImage2.jpg", store);
//
// 		menuList = new ArrayList<>();
// 		menuList.add(menu1);
// 		order = Order.createOf(testMember, store, menuList, OrderStatus.ADMISSION, 10000);
// 		cancelledOrder = Order.createOf(testMember, store, menuList, OrderStatus.CANCELLED, 10000);
// 		anotherOrder = Order.createOf(testMember, anotherStore, menuList, OrderStatus.WAITING, 10000);
//
// 		memberRepository.save(testMember);
// 		memberRepository.save(anotherMember);
// 		memberRepository.save(testOwner);
// 		memberRepository.save(anotherOwner);
// 		storeRepository.save(store);
// 		storeRepository.save(anotherStore);
// 		menuRepository.save(menu1);
// 		menuRepository.save(menu2);
// 		orderRepository.save(order);
// 		orderRepository.save(cancelledOrder);
// 		orderRepository.save(anotherOrder);
// 	}
//
// 	@Test
// 	@DisplayName("주문 생성로직: 정상 주문 생성 - 여유 테이블 있을 때")
// 	void createOrder_success() throws Exception {
//
// 		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), menu2.getId()), 30000);
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(orderRequest)))
// 			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isCreated())
// 			.andExpect(jsonPath("$.orderStatus").value("ADMISSION"))
// 			.andExpect(jsonPath("$.totalPrice").value(30000))
// 			.andExpect(jsonPath("$.menuNames", hasSize(2)))
// 			.andExpect(jsonPath("$.menuNames[0]").value("menu1"))
// 			.andExpect(jsonPath("$.menuNames[1]").value("menu2"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 생성로직: 정상 주문 생성 - 메뉴 없이 요청")
// 	void createOrder_NoMenus() throws Exception {
//
// 		OrderRequest orderRequest = new OrderRequest(List.of(), 0);
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(orderRequest)))
// 			.andExpect(status().isCreated())
// 			.andExpect(jsonPath("$.orderStatus").value("ADMISSION")) //현재 따로 IN_PROGRESS 인 사람이 없기 때문
// 			.andExpect(jsonPath("$.totalPrice").value(0))
// 			.andExpect(jsonPath("$.menuNames").isEmpty());
// 	}
//
// 	@Test
// 	@DisplayName("주문 생성로직: 존재하지 않는 가게로 주문 요청")
// 	void createOrder_NotFoundStore() throws Exception {
// 		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId()), 10000);
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/9999/orders")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(orderRequest)))
// 			.andExpect(status().isNotFound())
// 			.andExpect(jsonPath("$.code").value("NOT_FOUND_STORE"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 생성로직: 잘못된 메뉴 포함된 주문 요청")
// 	void createOrder_NotFoundMenu() throws Exception {
// 		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), 999L), 3000);
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(orderRequest)))
// 			.andExpect(status().isBadRequest())
// 			.andExpect(jsonPath("$.code").value("NOT_ORDER_THIS_STORE"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 생성로직: 결제 금액 불일치")
// 	void createOrder_PaymentNotMatched() throws Exception {
// 		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), menu2.getId()), 4000);
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(orderRequest)))
// 			.andExpect(status().isBadRequest())
// 			.andExpect(jsonPath("$.code").value("PAYED_NOT_EQUAL_BILL"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 생성로직: 여유 테이블이 없는 경우는 WAITING 상태로 생성")
// 	void createOrder_Waiting() throws Exception {
// 		for (int i = 0; i < 5; i++) {
// 			orderRepository.save(Order.createOf(testMember, store, List.of(), OrderStatus.IN_PROGRESS, 0));
// 		}
// 		OrderRequest orderRequest = new OrderRequest(List.of(menu1.getId(), menu2.getId()), 30000);
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(orderRequest)))
// 			.andExpect(status().isCreated())
// 			.andExpect(jsonPath("$.orderStatus").value("WAITING"))
// 			.andExpect(jsonPath("$.totalPrice").value(30000));
//
// 	}
//
// 	@Test
// 	@DisplayName("주문 취소 요청 로직 성공: CANCEL_REQUEST")
// 	void requestCancellationOrder_success() throws Exception {
// 		OrderCancelRequest request = new OrderCancelRequest(testMember.getId());
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(put("/stores/" + store.getId() + "/orders/" + order.getId())
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderStatus").value("CANCEL_REQUEST"))
// 			.andExpect(jsonPath("$.orderId").value(order.getId()));
// 	}
//
// 	@Test
// 	@DisplayName("주문 취소 요청 로직 실패 주문자 불일치: ONLY_ORDER_ALLOWED")
// 	void requestCancellationOrder_ONLY_ORDER_ALLOWED() throws Exception {
// 		setAuthentication(anotherMember);
//
// 		mockMvc.perform(put("/stores/" + store.getId() + "/orders/" + order.getId())
// 				.principal(() -> anotherMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new OrderCancelRequest(null))))
// 			.andExpect(status().isForbidden())
// 			.andExpect(jsonPath("$.code").value("ONLY_ORDER_ALLOWED"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 취소 요청 로직 실패 가게 불일치: NOT_ORDER_THIS_STORE")
// 	void requestCancellationOrder_NOT_ORDER_THIS_STORE() throws Exception {
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(put("/stores/" + anotherStore.getId() + "/orders/" + order.getId())
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new OrderCancelRequest(null))))
// 			.andExpect(status().isBadRequest())
// 			.andExpect(jsonPath("$.code").value("NOT_ORDER_THIS_STORE"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 취소 요청 로직 실패 주문 상태 오류: CANCELLED_COMPLETED_NOT_ALLOWED")
// 	void requestCancellationOrder_CANCELLED_COMPLETED_NOT_ALLOWED() throws Exception {
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(put("/stores/" + store.getId() + "/orders/" + cancelledOrder.getId())
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new OrderCancelRequest(null))))
// 			.andExpect(status().isBadRequest())
// 			.andExpect(jsonPath("$.code").value("CANCELLED_COMPLETED_NOT_ALLOWED"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 취소 요청 로직 실패 주문 ID 없을때: NOT_FOUND_ORDER")
// 	void requestCancellationOrder_NOT_FOUND_ORDER() throws Exception {
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(put("/stores/" + store.getId() + "/orders/99999")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new OrderCancelRequest(null))))
// 			.andExpect(status().isNotFound())
// 			.andExpect(jsonPath("$.code").value("NOT_FOUND_ORDER"));
// 	}
//
// 	@Test
// 	@DisplayName("자리선점 요청 테이블에 여유가 있을 경우 orderStatus 는 ADMISSION")
// 	void createWaiting_success_ADMISSION() throws Exception {
// 		setAuthentication(testMember);
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders/waiting")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new RequestDto(null))))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderStatus").value("ADMISSION"))
// 			.andExpect(jsonPath("$.waitingTeams").value(0));
// 	}
//
// 	@Test
// 	@DisplayName("자리선점 요청 테이블에 여유가 없는 경우 orderStatus는 WAITING")
// 	void createWaiting_success_WAITING() throws Exception {
// 		setAuthentication(testMember);
//
// 		for (int i = 0; i < store.getTableCount(); i++) {
// 			Order order = Order.createOf(testMember, store, List.of(), OrderStatus.IN_PROGRESS, 0);
// 			orderRepository.save(order);
// 		}
//
// 		mockMvc.perform(post("/stores/" + store.getId() + "/orders/waiting")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new RequestDto(null))))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderStatus").value("WAITING"))
// 			.andExpect(jsonPath("$.waitingTeams").value(1));
// 	}
//
// 	@Test
// 	@DisplayName("자리선점 요청 잘못된 storeId 로 요청할 경우 404 NOT_FOUND_STORE")
// 	void createWaiting_failed_NOT_FOUND_STORE() throws Exception {
// 		setAuthentication(testMember);
// 		Long invalidStoreId = 9999L;
//
// 		mockMvc.perform(post("/stores/" + invalidStoreId + "/orders/waiting")
// 				.principal(() -> testMember.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new RequestDto(null))))
// 			.andExpect(status().isNotFound())
// 			.andExpect(jsonPath("$.code").value("NOT_FOUND_STORE"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 상태 변경 성공 : 주문 상태를 ADMISSION -> IN_PROGRESS 로 변경한다.")
// 	void updateOrderStatus_success_IN_PROGRESS() throws Exception {
// 		setAuthentication(testOwner);
// 		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("IN_PROGRESS");
//
// 		mockMvc.perform(patch("/stores/" + store.getId() + "/orders/" + order.getId() + "/status")
// 				.principal(() -> testOwner.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderStatus").value("IN_PROGRESS"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 상태 변경 성공 : 주문 상태를 WAITING -> ADMISSION 으로 변경한다.")
// 	void updateOrderStatus_success_WAITING() throws Exception {
// 		setAuthentication(testOwner);
// 		order.changeStatus(OrderStatus.WAITING);
// 		orderRepository.save(order);
// 		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("ADMISSION");
//
// 		mockMvc.perform(patch("/stores/" + store.getId() + "/orders/" + order.getId() + "/status")
// 				.principal(() -> testOwner.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderStatus").value("ADMISSION"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 상태 변경 실패 : 존재하지 않는 가게 NOT_FOUND_STORE")
// 	void updateOrderStatus_failed_NOT_FOUND_STORE() throws Exception {
// 		setAuthentication(testOwner);
// 		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("IN_PROGRESS");
//
// 		mockMvc.perform(patch("/stores/9999/orders/" + order.getId() + "/status")
// 				.principal(() -> testOwner.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isNotFound())
// 			.andExpect(jsonPath("$.code").value("NOT_FOUND_STORE"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 상태 변경 실패 : 존재하지 않는 주문 NOT_FOUND_ORDER")
// 	void updateOrderStatus_failed_NOT_FOUND_ORDER() throws Exception {
// 		setAuthentication(testOwner);
// 		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("IN_PROGRESS");
//
// 		mockMvc.perform(patch("/stores/" + store.getId() + "/orders/9999/status")
// 				.principal(() -> testOwner.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isNotFound())
// 			.andExpect(jsonPath("$.code").value("NOT_FOUND_ORDER"));
// 	}
//
// 	@Test
// 	@DisplayName("주문 상태 변경 실패 : 가게 주인이 아닌 사용자가 요청 ONLY_OWNER_ALLOWED")
// 	void updateOrderStatus_failed_ONLY_OWNER_ALLOWED() throws Exception {
// 		setAuthentication(anotherOwner);
// 		OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("IN_PROGRESS");
//
// 		mockMvc.perform(patch("/stores/" + store.getId() + "/orders/" + order.getId() + "/status")
// 				.principal(() -> anotherOwner.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request)))
// 			.andExpect(status().isForbidden())
// 			.andExpect(jsonPath("$.code").value("ONLY_OWNER_ALLOWED"));
// 	}
//
// 	private void setAuthentication(Member member) {
// 		UserDetailsImpl authMember = new UserDetailsImpl(member);
// 		SecurityContextHolder.getContext().setAuthentication(
// 			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
// 		);
// 	}
// }