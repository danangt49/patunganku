package id.patunganku.settlement_service.service.impl;

import id.patunganku.settlement_service.domain.model.dto.SimplifiedTransactionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class GreedyDebtSimplifier {

    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(0.01);

    public List<SimplifiedTransactionDto> simplify(Map<String, BigDecimal> netBalances) {

        List<SimplifiedTransactionDto> result = new ArrayList<>();
        PriorityQueue<Map.Entry<String, BigDecimal>> creditors = new PriorityQueue<>((a, b) -> b.getValue().compareTo(a.getValue()));
        PriorityQueue<Map.Entry<String, BigDecimal>> debtors = new PriorityQueue<>(Map.Entry.comparingByValue());

        netBalances.forEach((name, balance) -> {

            if (balance.compareTo(THRESHOLD) > 0) {
                creditors.add(Map.entry(name, balance));

            } else if (balance.compareTo(THRESHOLD.negate()) < 0) {
                debtors.add(Map.entry(name, balance));
            }
        });

        while (!creditors.isEmpty() && !debtors.isEmpty()) {

            Map.Entry<String, BigDecimal> creditor = creditors.poll();
            Map.Entry<String, BigDecimal> debtor = debtors.poll();
            BigDecimal settleAmount = null;
            if (debtor != null) {
                settleAmount = creditor.getValue().min(debtor.getValue().abs());
            }

            if (debtor != null) {
                result.add(new SimplifiedTransactionDto(debtor.getKey(), creditor.getKey(), settleAmount));
            }

            BigDecimal remainingCredit = creditor.getValue().subtract(settleAmount);
            BigDecimal remainingDebt = null;
            if (debtor != null) {
                remainingDebt = debtor.getValue().abs().subtract(settleAmount);
            }
            if (remainingCredit.compareTo(THRESHOLD) > 0) {
                creditors.add(Map.entry(creditor.getKey(), remainingCredit));
            }

            if (remainingDebt != null && remainingDebt.compareTo(THRESHOLD) > 0) {
                debtors.add(Map.entry(debtor.getKey(), remainingDebt.negate()));
            }
        }

        return result;
    }
}