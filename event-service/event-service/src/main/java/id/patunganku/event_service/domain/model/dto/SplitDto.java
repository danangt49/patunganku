package id.patunganku.event_service.domain.model.dto;

import id.patunganku.event_service.domain.entity.ExpenseSplit;
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

    public static SplitDto from(ExpenseSplit split) {
        return SplitDto.builder()
                .memberId(split.getMember().getId())
                .memberName(split.getMember().getName())
                .shareAmount(split.getShareAmount())
                .build();
    }
}
