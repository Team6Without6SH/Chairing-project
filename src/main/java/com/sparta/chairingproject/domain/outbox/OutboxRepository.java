package com.sparta.chairingproject.domain.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.outbox.entity.Outbox;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
	List<Outbox> findByStatus(Outbox.Status status);
}
