package id.patunganku.settlement_service.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {
    private Long id;
    private Long paidByMemberId;
    private String paidByName;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private List<SplitDto> splits;
}
