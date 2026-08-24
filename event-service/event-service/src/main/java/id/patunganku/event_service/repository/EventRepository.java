package id.patunganku.event_service.repository;

import id.patunganku.event_service.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
