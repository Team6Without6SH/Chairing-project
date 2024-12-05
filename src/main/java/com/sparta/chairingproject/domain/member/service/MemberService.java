package com.sparta.chairingproject.domain.member.service;


import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.NOT_FOUND_USER;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.NOT_MATCH_PASSWORD;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.SAME_BEFORE_PASSWORD;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberOrderResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberReservationResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;

    public MemberResponse getMember(UserDetailsImpl authMember) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(() -> new GlobalException(NOT_FOUND_USER));
        return new MemberResponse(member.getEmail(), member.getName());
    }

    @Transactional
    public void updatePassword(UserDetailsImpl authMember, MemberPasswordRequest request) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new GlobalException(NOT_MATCH_PASSWORD);
        }

        if (passwordEncoder.matches(request.getUpdatePassword(), member.getPassword())) {
            throw new GlobalException(SAME_BEFORE_PASSWORD);
        }

        if (!request.getUpdatePassword().equals(request.getConfirmPassword())) {
            throw new GlobalException(NOT_MATCH_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(request.getUpdatePassword()));
    }


    public Page<MemberOrderResponse> getOrdersByMember(UserDetailsImpl authMember,
        RequestDto request, int page, int size) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Order> orders = orderRepository.findByMember(member.getId(), pageable);

        return orders.map(MemberOrderResponse::new);

    }

    public Page<MemberReservationResponse> getReservationsByMember(UserDetailsImpl authMember,
        RequestDto request, int page, int size) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(() -> new GlobalException(NOT_FOUND_MEMBER));

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Reservation> reservations = reservationRepository.findByMember(member.getId(),
            pageable);

        return reservations.map(MemberReservationResponse::new);
    }
}
