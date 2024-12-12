package com.sparta.chairingproject.domain.store.controller;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StoreControllerTest {

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private Member testOwner;
	private Store testStore;

	@BeforeEach
	void setUp() {

		storeRepository.deleteAll();
		memberRepository.deleteAll();

		//사장
		testOwner = Member.builder()
			.name("Test Owner")
			.email("owner@test.com")
			.password(passwordEncoder.encode("password"))
			.memberRole(MemberRole.OWNER)
			.build();
		memberRepository.save(testOwner);

		testStore = Store.builder()
			.name("Test Store")
			.address("Test Address")
			.phone("010-1234-5678")
			.openTime("09:00")
			.closeTime("18:00")
			.category("Cafe")
			.description("A test store")
			.owner(testOwner)
			.status(StoreStatus.OPEN)
			.requestStatus(StoreRequestStatus.APPROVED)
			.tableCount(10)
			.build();
		storeRepository.save(testStore);
		setAuthentication(testOwner, MemberRole.OWNER);
	}

	@Test
	@DisplayName("가게 등록 성공")
	void registerStore() throws Exception {
		StoreRequest storeRequest = new StoreRequest(
			"New Store",
			"New Address",
			"010-5678-1234",
			"10:00",
			"22:00",
			"Restaurant",
			"A new test store",
			null,
			15
		);

		mockMvc.perform(post("/owners/stores/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(storeRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("New Store"))
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andExpect(jsonPath("$.requestStatus").value("PENDING"));

		Store savedStore = storeRepository.findAll()
			.stream()
			.filter(store -> store.getName().equals("New Store"))
			.findFirst()
			.orElseThrow();

		assertThat(savedStore.getAddress()).isEqualTo("New Address");
		assertThat(savedStore.getTableCount()).isEqualTo(15);
	}

	@Test
	@DisplayName("가게 목록 조회")
	void getAllOpenedStores() throws Exception {
		mockMvc.perform(get("/members/stores")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()) // 200 상태 코드 확인
			.andExpect(jsonPath("$").isArray()) // 반환 값이 배열인지 확인
			.andExpect(jsonPath("$[0].name").value("Test Store")) // 올바른 값 확인
			.andExpect(jsonPath("$[0].status").value("OPEN")) // 상태 확인
			.andExpect(jsonPath("$[0].requestStatus").value("APPROVED"));
	}

	@Test
	@DisplayName("가게 상세 조회 성공")
	void getStoreDetails() throws Exception {
		// ROLE_USER 권한을 가진 사용자로 인증 설정
		Member testUser = Member.builder()
			.name("Test User")
			.email("user@test.com")
			.password(passwordEncoder.encode("password"))
			.memberRole(MemberRole.USER) // ROLE_USER 설정
			.build();
		memberRepository.save(testUser);

		setAuthentication(testUser, MemberRole.USER);

		mockMvc.perform(get("/stores/" + testStore.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Test Store"))
			.andExpect(jsonPath("$.description").value("A test store"))
			.andExpect(jsonPath("$.address").value("Test Address"))
			.andExpect(jsonPath("$.waitingCount").value(0));
	}

	@Test
	@DisplayName("가게 업데이트 성공")
	void updateStore() throws Exception {
		UpdateStoreRequest updateRequest = new UpdateStoreRequest(
			"Updated Store",
			"Updated Address",
			"010-1111-2222",
			"08:00",
			"20:00",
			"Updated Category",
			"Updated Description",
			null
		);

		mockMvc.perform(put("/owners/stores/" + testStore.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Updated Store"))
			.andExpect(jsonPath("$.address").value("Updated Address"));

		Store updatedStore = storeRepository.findById(testStore.getId()).orElseThrow();
		assertThat(updatedStore.getName()).isEqualTo("Updated Store");
		assertThat(updatedStore.getAddress()).isEqualTo("Updated Address");
	}

	@Test
	@DisplayName("가게 삭제 요청 성공")
	void requestDeleteStore() throws Exception {
		mockMvc.perform(post("/owners/stores/" + testStore.getId() + "/delete-request")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		Store deletedStore = storeRepository.findById(testStore.getId()).orElseThrow();
		assertThat(deletedStore.getRequestStatus()).isEqualTo(StoreRequestStatus.DELETE_REQUESTED);
	}

	@Test
	@DisplayName("가게 주문 목록 조회 성공")
	void getOrdersByStore() throws Exception {

		mockMvc.perform(get("/owners/stores/" + testStore.getId() + "/orders")
				.param("days", "7")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(0));
	}

	@Test
	@DisplayName("사장이 가게 상세 조회 성공")
	void getStore() throws Exception {
		mockMvc.perform(get("/owners/stores/" + testStore.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Test Store"))
			.andExpect(jsonPath("$.ownerId").value(testOwner.getId()));
	}

	private void setAuthentication(Member member, MemberRole role) {
		Member updatedMember = Member.builder()
			.id(member.getId())
			.name(member.getName())
			.email(member.getEmail())
			.password(member.getPassword())
			.memberRole(role) // 새로운 역할 설정
			.deleted(member.isDeleted())
			.deletedAt(member.getDeletedAt())
			.build();

		UserDetailsImpl userDetails = new UserDetailsImpl(member);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
			)
		);
	}
}
