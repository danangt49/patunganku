package id.patunganku.event_service.config.exception;

import id.patunganku.event_service.domain.model.dto.ErrorApiResponse;
import id.patunganku.event_service.util.UtilFn;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final transient ErrorApiResponse errorResponse;

    public CustomException(ErrorApiResponse errorResponse) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
    }

    public CustomException(HttpStatus httpStatus, String error, String message) {
        super(message);
        this.errorResponse = ErrorApiResponse.of(httpStatus.value(), error, message, UtilFn.getCurrentPath());
    }
}
