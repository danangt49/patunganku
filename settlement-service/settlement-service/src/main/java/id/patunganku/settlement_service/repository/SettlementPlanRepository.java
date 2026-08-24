package id.patunganku.settlement_service.repository;

import id.patunganku.settlement_service.domain.entity.SettlementPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementPlanRepository extends JpaRepository<SettlementPlan, Long> {
}
