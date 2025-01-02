package com.sparta.chairingproject.domain.outbox.entity;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.time.LocalDateTime;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.common.entity.OutboxEvent;
import com.sparta.chairingproject.domain.common.entity.Timestamped;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "outbox")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Outbox extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OutboxEvent.Type eventType;

	@Column(nullable = false)
	private Long userId;

	@Column
	private String token;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String body;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	protected LocalDateTime publishedAt;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime scheduledAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@Getter
	public enum Status {
		PENDING,
		PROCESSING,
		PUBLISHED,
		FAILED;

		public static Status fromString(String status) {
			try {
				return Status.valueOf(status.toUpperCase());
			} catch (Exception e) {
				throw new GlobalException(OUTBOX_EVENT_STATUS_NOT_FOUND);
			}
		}
	}

}