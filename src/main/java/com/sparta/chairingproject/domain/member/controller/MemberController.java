package com.sparta.chairingproject.domain.member.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberIssuanceResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberOrderResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberReservationResponse;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<MemberResponse> getMember(
        @AuthenticationPrincipal UserDetailsImpl authMember) {
        return ResponseEntity.ok(memberService.getMember(authMember));
    }

    @PatchMapping
    public void updatePassword(
        @Valid @RequestBody MemberPasswordRequest request,
        @AuthenticationPrincipal UserDetailsImpl authMember) {
        memberService.updatePassword(authMember, request);
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<MemberOrderResponse>> getOrdersByMember(
        @AuthenticationPrincipal UserDetailsImpl authMember,
        @RequestBody RequestDto request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(memberService.getOrdersByMember(authMember, request, page, size));
    }

    @GetMapping("/reservations")
    public ResponseEntity<Page<MemberReservationResponse>> getReservationsByMember(
        @AuthenticationPrincipal UserDetailsImpl authMember,
        @RequestBody RequestDto request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
            memberService.getReservationsByMember(authMember, request, page, size));
    }


    @GetMapping("/coupons")
    public ResponseEntity<Page<MemberIssuanceResponse>> getIssuanceByMember(
        @AuthenticationPrincipal UserDetailsImpl authMember,
        @RequestBody RequestDto request,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(
            memberService.getIssuanceByMember(authMember, request, page, size));
    }

}
