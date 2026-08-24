package id.patunganku.event_service.domain.entity.enumeric;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import id.patunganku.event_service.config.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum EventStatus {
    DONE,
    ACTIVE;

    @JsonCreator
    public static EventStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Event status cannot be null");
        }

        return switch (value.trim().toUpperCase()) {
            case "DONE" -> DONE;
            case "ACTIVE" -> ACTIVE;
            default -> throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_EVENT_STATUS",
                    "Invalid Event status: " + value + ". Allowed values: DONE, ACTIVE"
            );
        };
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
