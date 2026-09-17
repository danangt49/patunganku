package id.patunganku.event_service.domain.model.dto;

import id.patunganku.event_service.domain.entity.Event;
import id.patunganku.event_service.domain.entity.Member;
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
    private String createdByName;
    private Long createdAt;
    private EventStatus status;
    private List<MemberDto> members;

    public static EventDto from(Event event, List<MemberDto> memberDtos, String createdByName) {
        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .createdBy(event.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(event.getCreatedAt())
                .status(event.getStatus())
                .members(memberDtos)
                .build();
    }
}
