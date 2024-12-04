package com.sparta.chairingproject.domain.member.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.service.MemberService;
import com.sparta.chairingproject.util.ResponseBodyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseBodyDto<MemberResponse>> getMember(@AuthenticationPrincipal UserDetailsImpl authMember){
    return new ResponseEntity<>(
    ResponseBodyDto.success("내정보 조회 완료", memberService.getMember(authMember)),
    HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity<ResponseBodyDto> updatePassword(@RequestBody MemberPasswordRequest request,
        @AuthenticationPrincipal UserDetailsImpl authMember){
        memberService.updatePassword(authMember,request);
        return new ResponseEntity<>(ResponseBodyDto.success("비밀번호 수정 완료"),HttpStatus.OK);
    }
}
