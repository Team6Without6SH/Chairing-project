package com.sparta.chairingproject.domain.member.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.DELETE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberIssuanceResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberOrderResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberReservationResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;


class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private IssuanceRepository issuanceRepository;


    private Member member;
    private UserDetailsImpl authMember;
    private Store store;
    private Order order;
    private Reservation reservation;
    private Coupon coupon;
    private Menu menu;
    private List<Menu> menuList;
    private Issuance issuance;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        member = new Member(1L, "testName", "test@test.com", "encodedPassword", MemberRole.USER);
        authMember = new UserDetailsImpl(member);
        store = new Store(1L, "storeTest", "testImage", "storeDes", member);
        menu = new Menu(1L, "menuTest", 5000, "menuImage", false, store, false);
        menuList = List.of(menu);
        order = new Order(1L, member, store, menuList, OrderStatus.WAITING, 5000);
        reservation = new Reservation(1L, 1L, 5, LocalDate.now(), "12:00",
            ReservationStatus.PENDING, store);
        coupon = new Coupon(1L, "testCoupon", 100, 5000);
        issuance = new Issuance(1L, member, coupon);

    }

    @Test
    @DisplayName("내정보 조회 테스트")
    void getMember_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getMember(authMember);

        // then
        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
        assertEquals(member.getName(), response.getName());
        verify(memberRepository).findById(1L);
    }

    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    void updatePassword_Success() {
        // given
        MemberPasswordRequest request = new MemberPasswordRequest(
            "encodedPassword",
            "newPassword",
            "newPassword"
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(request.getUpdatePassword(), member.getPassword())).thenReturn(
            false);
        when(passwordEncoder.encode(request.getUpdatePassword())).thenReturn("encodedNewPassword");

        // when
        memberService.updatePassword(authMember, request);

        // then
        assertEquals("encodedNewPassword", member.getPassword());
        verify(passwordEncoder).encode(request.getUpdatePassword());

    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않을 때 예외를 발생시킨다.")
    void updatePassword_Fail_NotMatchOldPassword() {
        // given
        MemberPasswordRequest request = new MemberPasswordRequest("wrongPassword", "newPassword",
            "newPassword");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPassword())).thenReturn(
            false);

        // when, then
        assertThrows(GlobalException.class,
            () -> memberService.updatePassword(authMember, request));

        // verify
        verify(passwordEncoder).matches(request.getPassword(), member.getPassword());
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("현재 비밀번호와 변경 비밀번호가 같을 때 예외를 발생시킨다.")
    void updatePassword_Fail_SameOldAndNew() {
        // given
        MemberPasswordRequest request = new MemberPasswordRequest("correctPassword",
            "encodedPassword", "encodedPassword");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(request.getUpdatePassword(), member.getPassword())).thenReturn(
            true);

        // when, then
        assertThrows(GlobalException.class,
            () -> memberService.updatePassword(authMember, request));

        // verify
        verify(passwordEncoder).matches(request.getPassword(), member.getPassword());
        verify(passwordEncoder).matches(request.getUpdatePassword(), member.getPassword());
    }

    @Test
    @DisplayName("변경 비밀번호와 확인 비밀번호가 다를 때 예외를 발생시킨다.")
    void updatePassword_Fail_NotMatchNewAndConfirm() {
        // given
        MemberPasswordRequest request = new MemberPasswordRequest("encodedPassword", "newPassword",
            "mismatchPassword");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.getPassword(), member.getPassword())).thenReturn(true);

        // when, then
        assertThrows(GlobalException.class,
            () -> memberService.updatePassword(authMember, request));

        // verify
        verify(passwordEncoder).matches(request.getPassword(), member.getPassword());
    }


    @Test
    @DisplayName("내 주문 내역 조회 테스트")
    void getOrdersByMember_Success() {
        // given
        Page<Order> orders = new PageImpl<>(List.of(order));
        when(memberRepository.findById(eq(1L))).thenReturn(Optional.of(member));
        when(orderRepository.findByMember(eq(1L), any(Pageable.class))).thenReturn(orders);

        // when
        Page<MemberOrderResponse> result = memberService.getOrdersByMember(authMember,
            new RequestDto(), 1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository).findByMember(eq(1L), any(Pageable.class));
    }


    @Test
    @DisplayName("내 예약 내역 조회 테스트")
    void getReservationsByMember_Success() {
        // given
        Page<Reservation> reservations = new PageImpl<>(List.of(reservation));
        when(memberRepository.findById(eq(1L))).thenReturn(Optional.of(member));
        when(reservationRepository.findByMember(eq(1L), any(Pageable.class))).thenReturn(
            reservations);

        // when
        Page<MemberReservationResponse> result = memberService.getReservationsByMember(authMember,
            new RequestDto(), 1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reservationRepository).findByMember(eq(1L), any(Pageable.class));
    }


    @Test
    @DisplayName("내 쿠폰 조회 테스트")
    void getIssuanceByMember_Success() {
        // given
        Page<Issuance> issuances = new PageImpl<>(List.of(issuance));
        when(memberRepository.findById(eq(1L))).thenReturn(Optional.of(member));
        when(issuanceRepository.findByMember(eq(1L), any(Pageable.class))).thenReturn(issuances);

        // when
        Page<MemberIssuanceResponse> result = memberService.getIssuanceByMember(authMember,
            new RequestDto(), 1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(issuanceRepository).findByMember(eq(1L), any(Pageable.class));
    }


    @Test
    @DisplayName("회원 삭제 성공 테스트")
    void deleteMember_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        memberService.deleteMember(authMember, new RequestDto());

        // then
        assertEquals(true, member.isDeleted());
        verify(memberRepository).findById(1L);
    }

    @Test
    @DisplayName("회원 삭제 실패 테스트 - 이미 삭제된 회원")
    void deleteMember_Fail_AlreadyDeleted() {
        // given
        member.updateDelete(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when, then
        assertThrows(GlobalException.class,
            () -> memberService.deleteMember(authMember, new RequestDto()));
        verify(memberRepository).findById(1L);
    }


}
