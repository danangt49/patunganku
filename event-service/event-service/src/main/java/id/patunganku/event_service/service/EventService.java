package id.patunganku.event_service.service;

import id.patunganku.event_service.domain.model.dto.EventDto;
import id.patunganku.event_service.domain.model.dto.ExpenseDto;
import id.patunganku.event_service.domain.model.vo.EventVo;
import id.patunganku.event_service.domain.model.vo.ExpenseVo;

import java.util.Map;

public interface EventService {
    EventDto create(EventVo vo);
    EventDto getById(Long id);
    ExpenseDto addExpense(Long id, ExpenseVo vo);
    Map<String, Object> getSummary(Long id);
}
