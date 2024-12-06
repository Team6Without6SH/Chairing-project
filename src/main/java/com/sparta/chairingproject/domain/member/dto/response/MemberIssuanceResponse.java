package com.sparta.chairingproject.domain.member.dto.response;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import lombok.Getter;

@Getter
public class MemberIssuanceResponse {

    private final Long id;
    private final String name;
    private final int discountPrice;

    public MemberIssuanceResponse(Issuance issuance) {
        this.id = issuance.getId();
        this.name = issuance.getCoupon().getName();
        this.discountPrice = issuance.getCoupon().getDiscountPrice();

    }


}
