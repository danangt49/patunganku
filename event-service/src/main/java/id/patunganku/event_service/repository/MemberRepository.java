package id.patunganku.event_service.repository;

import id.patunganku.event_service.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByEventId(Long eventId);
    Optional<Member> findByIdAndEventId(Long id, Long eventId);
    Optional<Member> findByEventIdAndKeycloakUserId(Long eventId, String keycloakUserId);
}