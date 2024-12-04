package com.sparta.chairingproject.domain.member.service;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponseDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponseDto getMember(UserDetailsImpl authMember) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(()-> new InvalidRequestStateException("User not found"));
        return new MemberResponseDto(member.getEmail(),member.getName());
    }
}
