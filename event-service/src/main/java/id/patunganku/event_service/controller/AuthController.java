package id.patunganku.event_service.controller;

import id.patunganku.event_service.domain.model.dto.GetTokenDto;
import id.patunganku.event_service.domain.model.dto.GlobalApiResponse;
import id.patunganku.event_service.domain.model.vo.LoginVo;
import id.patunganku.event_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "Management Auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login")
    @PostMapping("login")
    public GlobalApiResponse<GetTokenDto> login(@RequestBody @Valid LoginVo vo) {
        return GlobalApiResponse.success(authService.login(vo));
    }
}
