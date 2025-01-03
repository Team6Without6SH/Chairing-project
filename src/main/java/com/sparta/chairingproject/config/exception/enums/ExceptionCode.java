package com.sparta.chairingproject.config.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ExceptionCode {

	//----------MEMBER----------
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다"),
	DELETED_USER(HttpStatus.BAD_REQUEST, "탈퇴한 멤버입니다"),
	NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "이름은 최대 4글자까지 가능합니다"),
	USERNAME_REQUIRED(HttpStatus.BAD_REQUEST, "이름이 누락되었습니다"),
	MEMBER_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 회원입니다"),
	INVALID_POINT_VALUE(HttpStatus.NOT_FOUND, "0보다 큰 정수를 입력하세요"),
	ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, "주소가 누락되었습니다"),

	//이메일
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "해당 이메일이 존재합니다"),
	EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "이메일이 누락되었습니다"),
	INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일입니다"),

	//패스워드
	PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "패스워드가 누락되었습니다"),
	NOT_MATCH_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 맞지 않습니다"),
	SAME_BEFORE_PASSWORD(HttpStatus.BAD_REQUEST, "변경된 비밀번호가 이전과 같습니다"),

	//----------가게----------
	STORE_OUT_OF_BUSINESS(HttpStatus.FORBIDDEN, "폐업한 가게입니다"),
	CANNOT_MODIFY_STORE_ID(HttpStatus.BAD_REQUEST, "가게 아이디는 수정할 수 없습니다"),
	NOT_FOUND_STORE(HttpStatus.NOT_FOUND, "해당 가게가 없습니다"),
	CANNOT_EXCEED_STORE_LIMIT(HttpStatus.FORBIDDEN, "최대 가게 3개만 소유 가능합니다."),
	UNAUTHORIZED_STORE_ACCESS(HttpStatus.FORBIDDEN, "해당 가게의 접근 권한이 없습니다."),
	NOT_FOUND_DATA(HttpStatus.NOT_FOUND, "가게 데이터를 찾을 수 없습니다."),

	/* --- 예약 --- */
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약을 찾을 수 없습니다."),
	RESERVATION_STATUS_NOT_FOUND(HttpStatus.BAD_REQUEST, "올바르지 않은 상태값입니다."),
	CANCELLATION_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "대기 중인 예약이 아니기 때문에 취소할 수 없습니다."),
	CANNOT_CANCEL_OTHERS_RESERVATION(HttpStatus.FORBIDDEN, "다른 사람의 예약은 취소할 수 없습니다."),
	CANNOT_REJECT_RESERVATION(HttpStatus.UNPROCESSABLE_ENTITY, "예약을 거절할 수 없습니다."),
	INVALID_STATUS_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY, "예약 상태를 변경할 수 없습니다."),

	//----------메뉴----------
	NOT_FOUND_MENU(HttpStatus.NOT_FOUND, "해당 메뉴가 없습니다"),
	MENU_ALREADY_DELETED(HttpStatus.NOT_FOUND, "이미 삭제된 메뉴입니다"),
	NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "해당 카테고리가 없습니다"),
	NOT_FOUND_OPTION(HttpStatus.NOT_FOUND, "해당 옵션이 없습니다"),
	OPTION_NOT_BELONG_TO_MENU(HttpStatus.NOT_FOUND, "해당 옵션은 선택한 메뉴에 포함되어 있지 않습니다"),
	DUPLICATED_MENU(HttpStatus.CONFLICT, "이미 존재하는 메뉴입니다."),
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "가격은 최소 100원 이상입니다."),
	ONLY_DELETED_MENU_CAN_BE_REMOVED(HttpStatus.FORBIDDEN, "삭제된 메뉴만 관리자 권한으로 최종삭제할 수 있습니다."),

	//----------주문----------
	STORE_CLOSED(HttpStatus.GONE, "영업 시간이 아닙니다"),
	LOWER_THAN_MIN_ORDER(HttpStatus.BAD_REQUEST, "주문 금액이 최소 주문 금액보다 작습니다"),
	NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "해당 주문이 없습니다"),

	CANNOT_CHANGE_TO_PENDING(HttpStatus.FORBIDDEN, "대기 상태로 변경할 수 없습니다"),
	ACCEPT_ONLY_PENDING(HttpStatus.FORBIDDEN, "대기 상태일때만 수락 가능합니다"),
	REJECTED_ONLY_PENDING(HttpStatus.FORBIDDEN, "대기 상태일때만 거절 가능합니다"),
	COMPLETED_ONLY_ACCEPT(HttpStatus.FORBIDDEN, "수락 상태일때만 완료 가능합니다"),
	ONLY_ORDER_ALLOWED(HttpStatus.FORBIDDEN, "주문한 사람만 수정 가능합니다"),
	CANCEL_ONLY_PENDING(HttpStatus.FORBIDDEN, "대기 상태일때만 취소 가능합니다"),
	ORDER_NOT_DELIVERED(HttpStatus.FORBIDDEN, "주문이 배달 완료 상태가 아닙니다"),
	STORE_CLOSED_BY_OWNER(HttpStatus.GONE, "해당 가게는 개인사정으로 문을 닫았습니다"),
	NOT_ORDER_THIS_STORE(HttpStatus.BAD_REQUEST, "선택한 메뉴는 주문할 수 없습니다."),
	NOT_REJECTED_ACCEPT(HttpStatus.BAD_REQUEST, "완료된 주문은 취소할 수 없습니다."),
	PAYED_NOT_EQUAL_BILL(HttpStatus.BAD_REQUEST, "총 가격과 결제 가격이 일치하지 않습니다."),
	CANCELLED_COMPLETED_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "완료되거나 이미 취소된 주문은 취소 요청할 수 없습니다."),
	CANCEL_REQUEST_NOT_ALLOWED_BY_OWNER(HttpStatus.FORBIDDEN, "가게 사장님은 취소 요청을 보낼 수 없습니다."),
	CANNOT_CHANGE_COMPLETED_OR_CANCELLED(HttpStatus.BAD_REQUEST,
		"완료되거나 이미 취소된 주문은 상태를 변경할 수 없습니다"), //사장님 전용
	TABLE_FULL_CANNOT_SET_IN_PROGRESS(HttpStatus.CONFLICT, "만석 상태에서는 진행중 상태로 변경할 수 없습니다."),
	CANCELLED_ADMISSION_ALLOWED_FROM_WAITING(HttpStatus.BAD_REQUEST, "웨이팅인 사람은 입장 또는 취소 상태로만 변경 시킬 수 있습니다."),
	IN_PROGRESS_CANCELLED_ALLOWED_FROM_ADMISSION(HttpStatus.BAD_REQUEST,
		"입장 상태에서는 진행중 또는 취소 상태로만 변경가능합니다."),
	NOT_VALID_STATUS_NAME(HttpStatus.BAD_REQUEST, "상태값이 잘못 입력되었습니다."),

	//----------리뷰----------
	NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "해당 리뷰가 없습니다"),
	SCORE_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "점수는 1 에서 5 사이 숫자에서 골라주세요"),
	REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 주문에 대해 이미 리뷰가 작성되었습니다"),
	NOT_ORDER_STATUS_COMPLETED(HttpStatus.FORBIDDEN, "주문이 배달 완료 상태가 아닙니다."),
	STORE_PENDING_CANNOT_REVIEW(HttpStatus.FORBIDDEN, "가게가 등록 대기 상태입니다. 리뷰를 작성할 수 없습니다."),
	ORDER_NOT_COMPLETED_CANNOT_REVIEW(HttpStatus.FORBIDDEN, "주문이 완료처리되지 않았습니다. 리류를 작성할 수 없습니다."),
	NOT_AUTHOR_OF_REVIEW(HttpStatus.UNAUTHORIZED, "해당 리뷰의 작성자가 아닙니다."),
	REVIEW_ALREADY_DELETED(HttpStatus.FORBIDDEN, "이미 삭제된 리뷰입니다."),

	//----------댓글----------
	NOT_MATCHING_STORE_AND_ORDER(HttpStatus.BAD_REQUEST, "리뷰가 요청한 가게의 주문과 연결되어 있지 않습니다. 댓글을 작성할 수 없습니다."),
	COMMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 리뷰에 대한 댓글을 작성했습니다."),
	NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	NOT_MATCHING_COMMENT_AND_REVIEW(HttpStatus.BAD_REQUEST, "댓글이 해당 리뷰에 연결되지 않았습니다."),
	NOT_AUTHOR_OF_COMMENT(HttpStatus.UNAUTHORIZED, "해당 댓글의 작성자가 아닙니다."),
	COMMENT_ALREADY_DELETED(HttpStatus.FORBIDDEN, "이미 삭제된 댓글입니다."),

	//----------쿠폰----------
	COUPON_OUT_OF_STOCK(HttpStatus.CONFLICT, "쿠폰 수량이 부족합니다."),
	COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 쿠폰을 찾을 수 없습니다."),
	COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 해당 쿠폰을 발급받았습니다."),
	COUPON_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 쿠폰 이름입니다."),
	LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT,"동시에 너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요."),

	//권한
	ONLY_OWNER_ALLOWED(HttpStatus.FORBIDDEN, "사장님만 가능합니다"),
	HAS_NOT_PERMISSION(HttpStatus.FORBIDDEN, "권한이 없습니다"),
	ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "OWNER과 USER중 하나를 입력하세요"),
	UNAUTHORIZED_OWNER(HttpStatus.UNAUTHORIZED, "해당 가게의 사장님만 가능합니다"),

	//토큰
	NOT_VALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 잘못되었거나 누락되었습니다"),

	//----------FCM----------
	INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 FCM 토큰입니다"),
	EXPIRED_FCM_TOKEN(HttpStatus.BAD_REQUEST, "만료된 FCM 토큰입니다"),
	NOT_FOUND_FCM_TOKEN(HttpStatus.NOT_FOUND, "FCM 토큰을 찾을 수 없습니다"),
	DUPLICATE_FCM_TOKEN(HttpStatus.CONFLICT, "이미 등록된 FCM 토큰입니다"),
	FAILED_TO_REGISTER_FCM_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 등록에 실패했습니다"),
	FAILED_TO_DELETE_FCM_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 삭제에 실패했습니다"),
	FAILED_TO_CLEAN_EXPIRED_TOKENS(HttpStatus.INTERNAL_SERVER_ERROR, "만료된 FCM 토큰 정리에 실패했습니다"),
	FCM_MESSAGE_SEND_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 메시지 전송에 실패했습니다"),

	/*-- outbox event --*/
	OUTBOX_EVENT_STATUS_NOT_FOUND(HttpStatus.BAD_REQUEST, "올바르지 않은 상태값입니다."),
	RESERVATION_EVENT_STATUS_NOT_FOUND(HttpStatus.BAD_REQUEST, "올바르지 않은 상태값입니다."),
	FAILED_TO_SERIALIZE_EVENT(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 직렬화에 실패했습니다"),

	HAS_NOT_COOKIE(HttpStatus.BAD_REQUEST, "Request has not cookie"),
	NOT_SUPPORT_ENCODING_COOKIE(HttpStatus.BAD_REQUEST, "Not support encoding cookie"),
	HAS_NOT_TOKEN(HttpStatus.BAD_REQUEST, "Request has not token"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Token is expired"),
	NOT_SUPPORT_TOKEN(HttpStatus.UNAUTHORIZED, "Is not support token"),
	INVALID_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 요청 상태입니다"),
	APPROVAL_PENDING(HttpStatus.CONFLICT, "승인 대기중입니다"),
	STORE_ALREADY_DELETED(HttpStatus.FORBIDDEN, "이미 삭제된 가게입니다.");

	private final HttpStatus httpStatus;
	private final String message;

	ExceptionCode(HttpStatus httpStatus, String message) {
		this.httpStatus = httpStatus;
		this.message = message;
	}

}
