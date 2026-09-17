package id.patunganku.settlement_service.service.impl;

import id.patunganku.settlement_service.config.client.EventClient;
import id.patunganku.settlement_service.config.exception.CustomException;
import id.patunganku.settlement_service.domain.entity.SettlementPlan;
import id.patunganku.settlement_service.domain.entity.SettlementTransaction;
import id.patunganku.settlement_service.domain.model.dto.*;
import id.patunganku.settlement_service.repository.SettlementPlanRepository;
import id.patunganku.settlement_service.repository.SettlementTransactionRepository;
import id.patunganku.settlement_service.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final EventClient eventClient;
    private final GreedyDebtSimplifier greedyDebtSimplifier;
    private final SettlementPlanRepository settlementPlanRepository;
    private final SettlementTransactionRepository settlementTransactionRepository;

    @Override
    public SettlementPlanDto generatePlan(Long eventId) {

        List<ExpenseDto> expenses = eventClient.getExpenses(eventId);
        Map<String, BigDecimal> netBalances = calculateNetBalances(expenses);
        Map<String, Long> nameToId = memberNameToId(expenses);
        List<SimplifiedTransactionDto> simplified = greedyDebtSimplifier.simplify(netBalances);

        SettlementPlan plan = SettlementPlan.builder()
                .eventId(eventId)
                .build();

        List<SettlementTransaction> transactions =
                simplified.stream()
                        .map(transaction ->
                                SettlementTransaction.builder()
                                        .plan(plan)
                                        .fromMemberId(nameToId.get(transaction.getFromName()))
                                        .fromMemberName(transaction.getFromName())
                                        .toMemberId(nameToId.get(transaction.getToName()))
                                        .toMemberName(transaction.getToName())
                                        .amount(transaction.getAmount())
                                        .build()
                        )
                        .collect(Collectors.toList());

        plan.setTransactions(transactions);

        SettlementPlan savedPlan = settlementPlanRepository.save(plan);

        return SettlementPlanDto.from(savedPlan);
    }

    @Override
    public SettlementPlanDto markAsPaid(Long planId, Long transactionId) {

        SettlementPlan plan = settlementPlanRepository.findById(planId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "SETTLEMENT_NOT_FOUND", "Settlement not found"));

        SettlementTransaction transaction = plan.getTransactions()
                .stream()
                .filter(t -> t.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "SETTLEMENT_TRANSACTION_NOT_FOUND", "Settlement transaction not found"));

        if (Boolean.TRUE.equals(transaction.getPaid())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "TRANSACTION_ALREADY_PAID", "Settlement transaction already paid");
        }

        transaction.setPaid(true);
        settlementTransactionRepository.save(transaction);

        return SettlementPlanDto.from(plan);
    }

    @Override
    public List<DebtorSummaryProjection> getUnpaidSummary() {
        return settlementTransactionRepository.findUnpaidDebtSummary();
    }

    private Map<String, BigDecimal> calculateNetBalances(List<ExpenseDto> expenses) {

        Map<String, BigDecimal> paidTotal = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseDto::getPaidByName,
                        Collectors.reducing(BigDecimal.ZERO, ExpenseDto::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> owedTotal = expenses.stream()
                .flatMap(e -> e.getSplits().stream())
                .collect(Collectors.groupingBy(
                        SplitDto::getMemberName,
                        Collectors.reducing(BigDecimal.ZERO, SplitDto::getShareAmount, BigDecimal::add)
                ));

        return Stream.concat(paidTotal.keySet().stream(), owedTotal.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> paidTotal.getOrDefault(name, BigDecimal.ZERO).subtract(owedTotal.getOrDefault(name, BigDecimal.ZERO))
                ));
    }

    private Map<String, Long> memberNameToId(List<ExpenseDto> expenses) {

        Map<String, Long> fromPaid = expenses.stream()
                .collect(Collectors.toMap(ExpenseDto::getPaidByName, ExpenseDto::getPaidByMemberId, (a, _) -> a));

        Map<String, Long> fromSplits = expenses.stream()
                .flatMap(e -> e.getSplits().stream())
                .collect(Collectors.toMap(SplitDto::getMemberName, SplitDto::getMemberId, (a, _) -> a));

        fromPaid.putAll(fromSplits);

        return fromPaid;
    }
}