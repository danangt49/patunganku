package id.patunganku.event_service.controller;

import id.patunganku.event_service.domain.model.dto.EventDto;
import id.patunganku.event_service.domain.model.dto.ExpenseDto;
import id.patunganku.event_service.domain.model.dto.GlobalApiResponse;
import id.patunganku.event_service.domain.model.vo.EventVo;
import id.patunganku.event_service.domain.model.vo.ExpenseVo;
import id.patunganku.event_service.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/event", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Event", description = "Management event")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "Create Event")
    @PostMapping
    public GlobalApiResponse<EventDto> create(@RequestBody @Valid EventVo vo) {
        return GlobalApiResponse.success(eventService.create(vo));
    }

    @Operation(summary = "Get detail event by id")
    @GetMapping("{id}")
    public GlobalApiResponse<EventDto> detail(@PathVariable Long id) {
        return GlobalApiResponse.success(eventService.getById(id));
    }

    @Operation(summary = "Get detail event by id")
    @PostMapping("{id}/expenses")
    public GlobalApiResponse<ExpenseDto> add(@PathVariable Long id, @Valid @RequestBody ExpenseVo vo) {
        return GlobalApiResponse.success(eventService.addExpense(id, vo));
    }

    @GetMapping("{id}/expenses/summary")
    public GlobalApiResponse<Map<String, Object>> summary(@PathVariable Long id) {
        return GlobalApiResponse.success(eventService.getSummary(id));
    }
}
