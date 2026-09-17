package id.patunganku.settlement_service.service;

import id.patunganku.settlement_service.domain.model.dto.DebtorSummaryProjection;
import id.patunganku.settlement_service.domain.model.dto.SettlementPlanDto;

import java.util.List;

public interface SettlementService {
    SettlementPlanDto generatePlan(Long eventId);
    SettlementPlanDto markAsPaid(Long planId, Long transactionId);
    List<DebtorSummaryProjection> getUnpaidSummary();
}
