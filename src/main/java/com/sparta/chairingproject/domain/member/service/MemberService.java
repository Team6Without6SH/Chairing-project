package com.sparta.chairingproject.domain.member.service;


import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.common.service.S3Uploader;
import com.sparta.chairingproject.domain.member.dto.request.CheckPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberIssuanceResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberOrderResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberReservationResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final OrderRepository orderRepository;
	private final ReservationRepository reservationRepository;
	private final IssuanceRepository issuanceRepository;

	private final S3Uploader s3Uploader;


	public MemberResponse getMemberDetails(UserDetailsImpl authMember) {
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
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		Pageable pageable = PageRequest.of(page - 1, size);

		Page<Reservation> reservations = reservationRepository.findByMember(member.getId(),
			pageable);

		return reservations.map(MemberReservationResponse::new);
	}

	public Page<MemberIssuanceResponse> getIssuanceByMember(UserDetailsImpl authMember,
		RequestDto request, int page, int size) {
		Member member = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		Pageable pageable = PageRequest.of(page - 1, size);

		Page<Issuance> issuance = issuanceRepository.findByMember(member.getId(), pageable);

		return issuance.map(MemberIssuanceResponse::new);

	}

	@Transactional
	public void deleteMember(UserDetailsImpl authMember, CheckPasswordRequest request) {
		Member member = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
			throw new GlobalException(NOT_MATCH_PASSWORD);
		}

		if (member.getDeletedAt() != null) {
			throw new GlobalException(DELETED_USER);
		}
		member.delete();

	}

	@Transactional
	public void updateImage(UserDetailsImpl authMember, MultipartFile file) {

		Member member = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		String fileName = s3Uploader.update(member.getImage(), file, "userProfile/");

		member.updateImage(fileName);
	}
}
