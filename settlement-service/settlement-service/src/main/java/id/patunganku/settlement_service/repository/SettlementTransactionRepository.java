package id.patunganku.settlement_service.repository;

import id.patunganku.settlement_service.domain.entity.SettlementTransaction;
import id.patunganku.settlement_service.domain.model.dto.DebtorSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, Long> {
    @Query(value = """
            SELECT st.from_member_id AS fromMemberId,
                   st.from_member_name AS fromMemberName,
                   SUM(st.amount) AS totalUnpaid
            FROM settlement_transaction st
            WHERE st.is_paid = false
            GROUP BY st.from_member_id, st.from_member_name
            ORDER BY totalUnpaid DESC
            """, nativeQuery = true)
    List<DebtorSummaryProjection> findUnpaidDebtSummary();
}