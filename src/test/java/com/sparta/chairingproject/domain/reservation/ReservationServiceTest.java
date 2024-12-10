package com.sparta.chairingproject.domain.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
import com.sparta.chairingproject.domain.reservation.service.ReservationService;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;
import com.sparta.chairingproject.util.AuthUtils;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

	@InjectMocks
	private ReservationService reservationService;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private StoreRepository storeRepository;

	@Spy
	private AuthUtils authUtils;

	@Mock
	private UserDetailsService userDetailsService;

	private Member member;
	private Long memberId = 1L;
	private UserDetailsImpl authMember;
	private Member owner;
	private Long ownerId = 1L;

	private Store store;
	private Long storeId = 1L;

	@BeforeEach
	void setUp() {
		member = new Member(1L, "Test User", "test@test.com", "encodedPassword", MemberRole.USER);
		authMember = new UserDetailsImpl(member);

		owner = new Member(2L, "Test Owner", "test2@example.com", "password", MemberRole.OWNER);

		store = new Store(1L, "Test Store", "Image", "설명", owner);

		storeRepository.save(store);
	}

	@Test
	@DisplayName("예약하기 성공")
	void createReservation_Success() {
		// given
		CreateReservationRequest req = new CreateReservationRequest(2, "2024-12-04", "12:00");
		Reservation expectedReservation = req.toEntity(memberId, store);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reservationRepository.save(any(Reservation.class))).thenReturn(expectedReservation);

		ReservationResponse res = reservationService.createReservation(storeId, req, authMember);

		// then
		assertNotNull(res);
		assertEquals(2, res.getGuestCount());
		assertEquals(storeId, res.getStoreId());
		assertEquals("PENDING", res.getStatus().toString());

		// 저장소 호출 확인
		verify(storeRepository, times(1)).findById(storeId);
		verify(reservationRepository, times(1)).save(any(Reservation.class));
	}

	@Test
	@DisplayName("해당 가게를 찾을 수 없는 경우")
	void createReservation_When_NOT_FOUND_STORE() {
		// given
		Long invalidStoreId = 10L;
		CreateReservationRequest req = new CreateReservationRequest(2, "2024-12-04", "12:00");

		// W T
		doReturn(member).when(authUtils).findAuthUser(req, authMember);

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.createReservation(invalidStoreId, req, authMember));

		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}
}
