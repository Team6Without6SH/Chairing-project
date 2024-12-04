package com.sparta.chairingproject.domain.member.service;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.dto.request.MemberPasswordRequest;
import com.sparta.chairingproject.domain.member.dto.response.MemberResponse;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse getMember(UserDetailsImpl authMember) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(() -> new InvalidRequestStateException("Member not found"));
        return new MemberResponse(member.getEmail(), member.getName());
    }

    @Transactional
    public void updatePassword(UserDetailsImpl authMember, MemberPasswordRequest request) {
        Member member = memberRepository.findById(authMember.getMember().getId())
            .orElseThrow(() -> new InvalidRequestStateException("Member not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidRequestStateException("기존 비밀번호가 다릅니다");
        }

        if (passwordEncoder.matches(request.getUpdatePassword(), member.getPassword())) {
            throw new InvalidRequestStateException("기존 비밀번호와 같습니다.");
        }

        if (!request.getUpdatePassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestStateException("비밀번호가 일치하지 않습니다");
        }

        member.updatePassword(passwordEncoder.encode(request.getUpdatePassword()));
    }
}
