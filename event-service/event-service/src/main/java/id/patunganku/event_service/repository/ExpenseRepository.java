package id.patunganku.event_service.repository;

import id.patunganku.event_service.domain.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByEventId(Long eventId);
}