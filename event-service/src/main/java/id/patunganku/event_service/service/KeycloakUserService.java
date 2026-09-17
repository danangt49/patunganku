package id.patunganku.event_service.service;

import id.patunganku.event_service.domain.model.dto.KeycloakUserDto;

public interface KeycloakUserService {
    KeycloakUserDto getUser(String userId);
    void validateUser(String userId);
    String getUserName(String userId);
}
