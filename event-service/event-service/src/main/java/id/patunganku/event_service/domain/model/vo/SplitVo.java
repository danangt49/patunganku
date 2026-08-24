package id.patunganku.event_service.domain.model.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitVo {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Share amount is required")
    @Positive(message = "Share amount must be greater than zero")
    private BigDecimal shareAmount;
}
