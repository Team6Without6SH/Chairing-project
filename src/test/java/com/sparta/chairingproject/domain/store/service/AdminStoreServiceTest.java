package com.sparta.chairingproject.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreAdminResponse;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;
import com.sparta.chairingproject.domain.member.entity.MemberRole;

@SpringBootTest
public class AdminStoreServiceTest {

	@Autowired
	private AdminStoreService adminStoreService;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private MemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		// Member 객체 생성 및 저장
		Member member = new Member(
			"testOwner",               // name
			"testOwner@example.com",   // email
			"password123",             // password
			 MemberRole.OWNER      // memberRole
		);
		memberRepository.save(member);

		// Store 객체 생성 및 저장
		Store store1 = new Store("Store1", "image1.jpg", "Description1", member);
		store1.approveRequest(); // 상태 변경
		storeRepository.save(store1);

		Store store2 = new Store("Store2", "image2.jpg", "Description2", member);
		storeRepository.save(store2);
	}

	@AfterEach
	void tearDown() {
		// 테스트 후 데이터 정리
		storeRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	@DisplayName("모든 가게 조회 테스트 - 가게 리스트 반환 확인")
	void testGetAllStores() {
		// 실행
		List<StoreAdminResponse> stores = adminStoreService.getAllStores();

		// 검증
		assertNotNull(stores);
		assertFalse(stores.isEmpty());
		assertEquals(2, stores.size());
	}

	@Test
	@DisplayName("단일 가게 조회 테스트 - ID로 가게 정보 가져오기")
	void testGetStoreById() {
		// 저장된 스토어 ID 가져오기
		Store store = storeRepository.findAll().get(0);

		// 실행
		StoreAdminResponse response = adminStoreService.getStoreById(store.getId());

		// 검증
		assertNotNull(response);
		assertEquals(store.getName(), response.getName());
		assertEquals(store.getOwner().getName(), response.getOwnerName());
	}
}
