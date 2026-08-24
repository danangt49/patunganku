package id.patunganku.event_service.domain.model.dto;

import id.patunganku.event_service.domain.entity.Event;
import id.patunganku.event_service.domain.entity.enumeric.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String name;
    private String createdBy;
    private EventStatus status;
    private LocalDateTime createdAt;
    private List<MemberDto> members;

    public static EventDto from(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .createdBy(event.getCreatedBy())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .members(event.getMembers().stream().map(MemberDto::from).collect(Collectors.toList()))
                .build();
    }
}
