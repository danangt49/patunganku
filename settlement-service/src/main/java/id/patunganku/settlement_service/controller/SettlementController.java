package id.patunganku.settlement_service.controller;

import id.patunganku.settlement_service.domain.model.dto.DebtorSummaryProjection;
import id.patunganku.settlement_service.domain.model.dto.GlobalApiResponse;
import id.patunganku.settlement_service.domain.model.dto.SettlementPlanDto;
import id.patunganku.settlement_service.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/settlement", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "settlement", description = "Management settlement")
public class SettlementController  {
    private final SettlementService settlementService;

    @Operation(summary = "Generate settlement plan by event id")
    @PostMapping("/generate/{eventId}")
    public GlobalApiResponse<SettlementPlanDto> generate(@PathVariable Long eventId) {
        return GlobalApiResponse.success(settlementService.generatePlan(eventId));
    }

    @Operation(summary = "Mark settlement transaction as paid")
    @PatchMapping("/{planId}/transactions/{transactionId}/pay")
    public GlobalApiResponse<SettlementPlanDto> markAsPaid(@PathVariable Long planId, @PathVariable Long transactionId) {
        return GlobalApiResponse.success(settlementService.markAsPaid(planId, transactionId));
    }

    @Operation(summary = "Get unpaid debt summary")
    @GetMapping("/reports/unpaid-summary")
    public GlobalApiResponse<List<DebtorSummaryProjection>> unpaidSummary() {
        return GlobalApiResponse.success(settlementService.getUnpaidSummary());
    }
}
