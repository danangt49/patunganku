package id.patunganku.event_service.domain.model.dto;

import id.patunganku.event_service.domain.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ExpenseDto from(Expense expense) {
        return ExpenseDto.builder()
                .id(expense.getId())
                .paidByMemberId(expense.getPaidBy().getId())
                .paidByName(expense.getPaidBy().getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .splits(expense.getSplits().stream().map(SplitDto::from).collect(Collectors.toList()))
                .build();
    }

}
