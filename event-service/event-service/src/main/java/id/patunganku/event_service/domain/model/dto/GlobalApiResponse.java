package id.patunganku.event_service.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalApiResponse<T> {
    @Builder.Default
    private boolean success = true;

    private String message;
    private T data;

    private Map<String, Object> page;

    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    public static <T> GlobalApiResponse<T> success(String message, T data) {
        return GlobalApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> GlobalApiResponse<T> success(T data) {
        return GlobalApiResponse.<T>builder()
            .success(true)
            .message("success")
            .data(data)
            .build();
    }

    public static <T> GlobalApiResponse<T> successWithPage(Page<T> pageData) {
        Map<String, Object> pageInfo = new LinkedHashMap<>();
        pageInfo.put("data", pageData.getContent());
        pageInfo.put("currentPage", pageData.getNumber());
        pageInfo.put("pageSize", pageData.getSize());
        pageInfo.put("totalPages", pageData.getTotalPages());
        pageInfo.put("totalRecords", pageData.getTotalElements());
        pageInfo.put("nextPage", pageData.hasNext());
        pageInfo.put("previousPage", pageData.hasPrevious());

        return GlobalApiResponse.<T>builder()
                .success(true)
                .message("success")
                .page(pageInfo)
                .build();
    }
}
