package id.patunganku.settlement_service.domain.model.dto;

import id.patunganku.settlement_service.domain.entity.SettlementTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long id;
    private Long fromMemberId;
    private String fromMemberName;
    private Long toMemberId;
    private String toMemberName;
    private BigDecimal amount;
    private Boolean isPaid;
    private java.time.LocalDateTime paidAt;

    public static TransactionDto from(SettlementTransaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getFromMemberId(),
                transaction.getFromMemberName(),
                transaction.getToMemberId(),
                transaction.getToMemberName(),
                transaction.getAmount(),
                transaction.getIsPaid(),
                transaction.getPaidAt()
        );
    }
}
