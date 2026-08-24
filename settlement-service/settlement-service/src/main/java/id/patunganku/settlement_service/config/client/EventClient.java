package id.patunganku.settlement_service.config.client;

import id.patunganku.settlement_service.config.exception.CustomException;
import id.patunganku.settlement_service.domain.model.dto.ExpenseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventClient {

    private final WebClient webClient;

    public List<ExpenseDto> getExpenses(Long eventId) {
        try {
            ExpenseDto[] response = webClient.get()
                    .uri("/api/events/{eventId}/expenses", eventId)
                    .retrieve()
                    .bodyToMono(ExpenseDto[].class)
                    .block();
            return response != null ? List.of(response) : List.of();
        } catch (WebClientException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Failed get data");
        }
    }
}