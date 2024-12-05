package com.sparta.chairingproject.domain.member.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
        @RequestBody MemberPasswordRequest request,
        @AuthenticationPrincipal UserDetailsImpl authMember) {
        memberService.updatePassword(authMember, request);
    }
}
