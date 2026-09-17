package id.patunganku.settlement_service.domain.model.dto;

import java.math.BigDecimal;

public interface DebtorSummaryProjection {
    Long getFromMemberId();
    String getFromMemberName();
    BigDecimal getTotalUnpaid();
}
