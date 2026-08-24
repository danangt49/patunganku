package id.patunganku.settlement_service.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitDto {
    private Long memberId;
    private String memberName;
    private BigDecimal shareAmount;
}
