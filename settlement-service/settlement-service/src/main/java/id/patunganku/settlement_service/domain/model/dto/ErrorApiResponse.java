package id.patunganku.settlement_service.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorApiResponse {
    @Builder.Default
    private boolean success = false;

    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;

    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    public static ErrorApiResponse of(int status, String error, String message, String path) {
        return ErrorApiResponse.builder()
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .build();
    }

    public static ErrorApiResponse of(int status, String error, String message, String path, List<String> details) {
        return ErrorApiResponse.builder()
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .details(details)
            .build();
    }
}
