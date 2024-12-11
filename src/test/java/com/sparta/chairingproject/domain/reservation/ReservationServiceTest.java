package com.sparta.chairingproject.domain.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.request.UpdateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
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
	private UserDetailsImpl authOwner;

	private Store store;
	private Long storeId = 1L;

	private Reservation reservation;
	private Long reservationId = 1L;

	@BeforeEach
	void setUp() {
		member = new Member("Test User", "test@test.com", "encodedPassword", MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", memberId);
		authMember = new UserDetailsImpl(member);

		owner = new Member("Test Owner", "test2@example.com", "password", MemberRole.OWNER);
		ReflectionTestUtils.setField(owner, "id", ownerId);
		authOwner = new UserDetailsImpl(owner);

		store = new Store("Test Store", "Image", "설명", "주소", owner);
		ReflectionTestUtils.setField(store, "id", storeId);

		storeRepository.save(store);

		reservation = Reservation.builder()
			.id(1L)
			.memberId(1L)
			.store(store)
			.guestCount(4)
			.date(LocalDate.of(2024, 12, 5))
			.time("18:00")
			.status(ReservationStatus.PENDING)
			.build();
	}

	@Test
	@DisplayName("예약하기 / 성공")
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
	@DisplayName("예약하기 / 해당 가게를 찾을 수 없는 경우")
	void createReservation_When_NOT_FOUND_STORE() {
		// given
		Long invalidStoreId = 10L;
		CreateReservationRequest req = new CreateReservationRequest(2, "2024-12-04", "12:00");

		// W T
		when(storeRepository.findById(invalidStoreId)).thenReturn(Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.createReservation(invalidStoreId, req, authMember));

		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 상태 변경 / 성공")
	void updateReservation_Success() {
		// given
		UpdateReservationRequest req = new UpdateReservationRequest();
		ReflectionTestUtils.setField(req, "status", "approved");

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ReservationResponse res = reservationService.updateReservation(storeId, reservationId, req, authMember);

		// then
		assertNotNull(res);
		assertEquals(4, res.getGuestCount());
		assertEquals(storeId, res.getStoreId());
		assertEquals(reservationId, res.getId());
		assertEquals("APPROVED", res.getStatus().toString());

		// 저장소 호출 확인
		verify(storeRepository, times(1)).findById(storeId);
		verify(reservationRepository, times(1)).findById(reservationId);
	}

	@Test
	@DisplayName("예약 상태 변경 / 해당 가게를 찾을 수 없는 경우")
	void updateReservation_When_NOT_FOUND_STORE() {
		// given
		Long invalidStoreId = 10L;

		UpdateReservationRequest req = new UpdateReservationRequest();
		ReflectionTestUtils.setField(req, "status", "approved");

		// W T
		when(storeRepository.findById(invalidStoreId)).thenReturn(Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.updateReservation(invalidStoreId, reservationId, req, authMember));

		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 상태 변경 / 해당 예약을 찾을 수 없는 경우")
	void updateReservation_When_RESERVATION_NOT_FOUND() {
		// given
		Long invalidReservationId = 10L;

		UpdateReservationRequest req = new UpdateReservationRequest();
		ReflectionTestUtils.setField(req, "status", "approved");

		// W T
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reservationRepository.findById(invalidReservationId)).thenReturn(Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.updateReservation(storeId, invalidReservationId, req, authMember));

		assertEquals(ExceptionCode.RESERVATION_NOT_FOUND, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 상태 변경 / 바꾸고자 하는 상태 이름이 올바르지 않을 경우")
	void updateReservation_When_RESERVATION_STATUS_NOT_FOUND() {
		// given
		UpdateReservationRequest req = new UpdateReservationRequest();
		ReflectionTestUtils.setField(req, "status", "default");

		// W T
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.updateReservation(storeId, reservationId, req, authMember));

		assertEquals(ExceptionCode.RESERVATION_STATUS_NOT_FOUND, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 상태 변경 / 예약 상태를 변경할 수 없는 경우")
	void updateReservation_When_CANNOT_REJECT_RESERVATION() {
		// given
		ReflectionTestUtils.setField(reservation, "status", ReservationStatus.APPROVED);

		UpdateReservationRequest req = new UpdateReservationRequest();
		ReflectionTestUtils.setField(req, "status", "pending");

		// W T
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.updateReservation(storeId, reservationId, req, authMember));

		assertEquals(ExceptionCode.INVALID_STATUS_TRANSITION, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 취소 / 성공")
	void cancelReservation_Success() {
		// given
		RequestDto req = new RequestDto();

		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ReservationResponse res = reservationService.cancelReservation(reservationId, req, authMember);

		// then
		assertNotNull(res);
		assertEquals(4, res.getGuestCount());
		assertEquals(storeId, res.getStoreId());
		assertEquals(reservationId, res.getId());
		assertEquals("CANCELED", res.getStatus().toString());

		// 저장소 호출 확인
		verify(reservationRepository, times(1)).findById(reservationId);
	}

	@Test
	@DisplayName("예약 취소 / 해당 예약을 찾을 수 없는 경우")
	void cancelReservation_RESERVATION_NOT_FOUND() {
		// given
		Long invalidReservationId = 10L;

		RequestDto req = new RequestDto();

		// W T
		when(reservationRepository.findById(invalidReservationId)).thenReturn(Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.cancelReservation(invalidReservationId, req, authMember));

		assertEquals(ExceptionCode.RESERVATION_NOT_FOUND, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 취소 / 내 예약이 아닌 경우")
	void cancelReservation_CANNOT_CANCEL_OTHERS_RESERVATION() {
		// given
		Long invalidMemberId = 10L;
		ReflectionTestUtils.setField(reservation, "memberId", invalidMemberId);

		RequestDto req = new RequestDto();

		// W T
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.cancelReservation(reservationId, req, authMember));

		assertEquals(ExceptionCode.CANNOT_CANCEL_OTHERS_RESERVATION, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 취소 / 예약이 진행 중이 아닐 경우")
	void cancelReservation_CANCELLATION_NOT_ALLOWED() {
		// given
		ReflectionTestUtils.setField(reservation, "status", ReservationStatus.APPROVED);

		RequestDto req = new RequestDto();

		// W T
		when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.cancelReservation(reservationId, req, authMember));

		assertEquals(ExceptionCode.CANCELLATION_NOT_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 다건 조회 / 성공")
	void getReservationList_Success() {
		// given
		Pageable pageable = PageRequest.of(0, 10);

		Page<Reservation> reservationPage = new PageImpl<>(
			List.of(reservation), pageable, 1
		);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reservationRepository.findByStoreId(storeId, pageable)).thenReturn(reservationPage);

		Page<ReservationResponse> res = reservationService.getReservationList(storeId, 0, 10, authOwner);

		// then
		assertNotNull(res);
		assertEquals(4, res.getContent().get(0).getGuestCount());
		assertEquals(storeId, res.getContent().get(0).getStoreId());
		assertEquals(reservationId, res.getContent().get(0).getId());
		assertEquals("PENDING", res.getContent().get(0).getStatus().toString());

		// 저장소 호출 확인
		verify(storeRepository, times(1)).findById(storeId);
		verify(reservationRepository, times(1)).findByStoreId(storeId, pageable);
	}

	@Test
	@DisplayName("예약 상태 변경 / 해당 가게를 찾을 수 없는 경우")
	void getReservationList_When_NOT_FOUND_STORE() {
		// given
		Long invalidStoreId = 10L;

		// W T
		when(storeRepository.findById(invalidStoreId)).thenReturn(Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.getReservationList(invalidStoreId, 0, 10, authOwner));

		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("예약 상태 변경 / 내 가게가 아닌 경우")
	void getReservationList_When_UNAUTHORIZED_STORE_ACCESS() {
		// given
		Member owner2 = new Member("Test Owner2", "test3@example.com", "password", MemberRole.OWNER);
		ReflectionTestUtils.setField(owner2, "id", 10L);

		ReflectionTestUtils.setField(store, "owner", owner2);

		// W T
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> reservationService.getReservationList(storeId, 0, 10, authOwner));

		assertEquals(ExceptionCode.UNAUTHORIZED_STORE_ACCESS, exception.getExceptionCode());
	}

}
