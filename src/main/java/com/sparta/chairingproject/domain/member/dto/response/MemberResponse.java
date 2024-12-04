package com.sparta.chairingproject.domain.member.dto.response;

import lombok.Getter;

@Getter
public class MemberResponse {
    private final String email;
    private final String name;

    public MemberResponse(String email, String name) {
        this.email = email;
        this.name = name;
    }

}
