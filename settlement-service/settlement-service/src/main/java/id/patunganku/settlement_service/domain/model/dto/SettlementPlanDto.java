package id.patunganku.settlement_service.domain.model.dto;

import id.patunganku.settlement_service.domain.entity.SettlementPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementPlanDto {
    private Long id;
    private Long eventId;
    private LocalDateTime generatedAt;
    private List<TransactionDto> transactions;

    public static SettlementPlanDto from(SettlementPlan plan) {
        return new SettlementPlanDto(
                plan.getId(),
                plan.getEventId(),
                plan.getGeneratedAt(),
                plan.getTransactions()
                        .stream()
                        .map(TransactionDto::from)
                        .collect(Collectors.toList())
        );
    }
}
