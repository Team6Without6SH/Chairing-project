package com.sparta.chairingproject.domain.member.dto.response;


import lombok.Getter;

@Getter
public class MemberOrderResponse {

    private Long id;
    private String status;
    private int price;
    private String storeName;

}
