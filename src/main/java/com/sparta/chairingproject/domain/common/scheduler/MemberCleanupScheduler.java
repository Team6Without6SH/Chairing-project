package com.sparta.chairingproject.domain.common.scheduler;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupMembers() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Member> membersDelete = memberRepository.findMembersToDelete(cutoffDate);

        if (!membersDelete.isEmpty()) {
            memberRepository.deleteAll(membersDelete);
        }
    }
}
