package id.patunganku.settlement_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(schema = "public", name = "settlement_transaction")
public class SettlementTransaction extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SettlementPlan plan;

    @Column(name = "from_member_id", nullable = false)
    private Long fromMemberId;

    @Column(name = "from_member_name", nullable = false, length = 100)
    private String fromMemberName;

    @Column(name = "to_member_id", nullable = false)
    private Long toMemberId;

    @Column(name = "to_member_name", nullable = false, length = 100)
    private String toMemberName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(name = "paid_at")
    private Long paidAt = System.currentTimeMillis();
}