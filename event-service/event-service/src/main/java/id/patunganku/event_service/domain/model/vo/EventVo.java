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

    @NotBlank(message = "Event Created By is required")
    private String createdBy;

    @NotEmpty(message = "Member Name is required")
    private List<String> memberNames;
}
