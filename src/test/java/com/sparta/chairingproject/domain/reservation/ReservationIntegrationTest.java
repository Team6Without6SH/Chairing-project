// package com.sparta.chairingproject.domain.reservation;
//
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.time.LocalDate;
// import java.util.List;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.ResultActions;
// import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
// import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
// import com.sparta.chairingproject.domain.reservation.dto.request.UpdateReservationRequest;
// import com.sparta.chairingproject.domain.reservation.entity.Reservation;
// import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
// import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
// import com.sparta.chairingproject.domain.store.entity.Store;
// import com.sparta.chairingproject.domain.store.repository.StoreRepository;
// import com.sparta.chairingproject.util.AuthUtils;
//
// @SpringBootTest
// @AutoConfigureMockMvc
// @Transactional
// class ReservationIntegrationTest {
//
// 	@Autowired
// 	private MockMvc mockMvc;
//
// 	@Autowired
// 	private WebApplicationContext context;
//
// 	@Autowired
// 	private ObjectMapper objectMapper;
//
// 	@Autowired
// 	private ReservationRepository reservationRepository;
//
// 	@Autowired
// 	private StoreRepository storeRepository;
//
// 	@Autowired
// 	private MemberRepository memberRepository;
//
// 	@Autowired
// 	private AuthUtils authUtils;
//
// 	@Autowired
// 	private PasswordEncoder passwordEncoder;
//
// 	private Member member;
// 	private Long memberId;
// 	//private UserDetailsImpl authMember;
// 	private Member owner;
// 	private Long ownerId;
// 	private Store store;
// 	private Long storeId;
// 	private Reservation reservation;
// 	private Long reservationId;
//
// 	@BeforeEach
// 	void setUp() {
// 		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
// 		objectMapper = new ObjectMapper();
//
// 		member = new Member("Test User", "test@test.com", passwordEncoder.encode("encodedPassword"), MemberRole.USER);
// 		memberId = memberRepository.save(member).getId();
//
// 		owner = new Member("Test Owner", "test2@example.com", "password", MemberRole.OWNER);
// 		ownerId = memberRepository.save(owner).getId();
//
// 		store = new Store("Test Store", "Image", "설명", "주소", owner);
// 		storeId = storeRepository.save(store).getId();
//
// 		reservation = Reservation.builder()
// 			.memberId(memberId)
// 			.store(store)
// 			.guestCount(4)
// 			.date(LocalDate.of(2024, 12, 5))
// 			.time("18:00")
// 			.status(ReservationStatus.PENDING)
// 			.build();
//
// 		reservationId = reservationRepository.save(reservation).getId();
// 	}
//
// 	@Test
// 	@DisplayName("일반 유저 / 예약하기")
// 	void createReservation() throws Exception {
// 		setUserAuthentication();
//
// 		CreateReservationRequest request = new CreateReservationRequest(4, "2024-12-11", "12:00");
//
// 		mockMvc.perform(post("/stores/" + storeId + "/reservations")
// 				.principal(() -> member.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request))
// 				.accept(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.guestCount").value(request.getGuestCount()))
// 			.andExpect(jsonPath("$.date").value(request.getDate()));
// 	}
//
// 	@Test
// 	@DisplayName("일반 유저 / 예약 취소")
// 	void cancelReservation() throws Exception {
// 		setUserAuthentication();
//
// 		RequestDto request = new RequestDto();
//
// 		mockMvc.perform(delete("/members/reservations/" + reservationId)
// 				.principal(() -> member.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request))
// 				.accept(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.id").value(reservationId))
// 			.andExpect(jsonPath("$.guestCount").value(reservation.getGuestCount()))
// 			.andExpect(jsonPath("$.date").value(reservation.getDate().toString()))
// 			.andExpect(jsonPath("$.status").value(ReservationStatus.CANCELED.name()));
// 	}
//
// 	@Test
// 	@DisplayName("사장 / 예약 상태 변경")
// 	void updateReservation() throws Exception {
// 		setOwnerAuthentication();
//
// 		UpdateReservationRequest request = new UpdateReservationRequest();
// 		request.setStatus("rejected");
//
// 		mockMvc.perform(patch("/stores/" + storeId + "/reservations/" + reservationId)
// 				.principal(() -> member.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(request))
// 				.accept(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.id").value(reservationId))
// 			.andExpect(jsonPath("$.status").value(ReservationStatus.REJECTED.name()));
// 	}
//
// 	@Test
// 	@DisplayName("사장 / 예약 다건 조회")
// 	void getReservationList() throws Exception {
// 		setOwnerAuthentication();
//
// 		reservationRepository.saveAll(List.of(
// 			Reservation.builder()
// 				.memberId(member.getId())
// 				.store(store)
// 				.guestCount(3)
// 				.date(LocalDate.now())
// 				.time("10:00")
// 				.status(ReservationStatus.PENDING)
// 				.build(),
// 			Reservation.builder()
// 				.memberId(member.getId())
// 				.store(store)
// 				.guestCount(4)
// 				.date(LocalDate.now())
// 				.time("11:00")
// 				.status(ReservationStatus.CANCELED)
// 				.build()
// 		));
//
// 		mockMvc.perform(get("/stores/" + storeId + "/reservations")
// 				.principal(() -> owner.getEmail())
// 				.contentType(MediaType.APPLICATION_JSON)
// 				//.content()
// 				.accept(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.content.length()").value(3))
// 			.andExpect(jsonPath("$.content[1].status").value(ReservationStatus.PENDING.name()))
// 			.andExpect(jsonPath("$.content[2].status").value(ReservationStatus.CANCELED.name()));
// 	}
//
// 	private void setUserAuthentication() {
// 		UserDetailsImpl authMember = new UserDetailsImpl(member);
// 		SecurityContextHolder.getContext().setAuthentication(
// 			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
// 		);
// 	}
//
// 	private void setOwnerAuthentication() {
// 		UserDetailsImpl authMember = new UserDetailsImpl(owner);
// 		SecurityContextHolder.getContext().setAuthentication(
// 			new UsernamePasswordAuthenticationToken(authMember, null, authMember.getAuthorities())
// 		);
// 	}
// }
