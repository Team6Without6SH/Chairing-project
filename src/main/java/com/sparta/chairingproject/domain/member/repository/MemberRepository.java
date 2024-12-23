package com.sparta.chairingproject.domain.member.repository;

import com.sparta.chairingproject.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.deletedAt != null AND m.modifiedAt <= :cutoffDate")
    List<Member> findMembersToDelete(@Param("cutoffDate") LocalDateTime cutoffDate);

}
