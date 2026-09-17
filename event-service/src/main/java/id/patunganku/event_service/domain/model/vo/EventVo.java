package id.patunganku.event_service.domain.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventVo {

    @NotBlank(message = "Event Name is required")
    private String name;

    @NotEmpty(message = "Member is required")
    private List<String> memberUserIds;
}
