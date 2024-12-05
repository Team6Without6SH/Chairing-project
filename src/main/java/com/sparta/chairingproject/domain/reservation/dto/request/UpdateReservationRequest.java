package com.sparta.chairingproject.domain.reservation.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UpdateReservationRequest extends RequestDto {

    @NotEmpty(message = "예약 상태는 필수 입력 항목입니다.")
    private String status;
}
