package com.sparta.chairingproject.domain.comment.dto;

import com.sparta.chairingproject.domain.common.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentRequest extends RequestDto {
	@NotBlank(message = "댓글 내용은 비어있을 수 없습니다.")
	private String content;
}
