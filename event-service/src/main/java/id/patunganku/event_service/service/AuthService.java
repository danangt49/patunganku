package id.patunganku.event_service.service;

import id.patunganku.event_service.domain.model.dto.GetTokenDto;
import id.patunganku.event_service.domain.model.vo.LoginVo;
import jakarta.validation.Valid;

public interface AuthService {
    GetTokenDto login(@Valid LoginVo vo);
}
