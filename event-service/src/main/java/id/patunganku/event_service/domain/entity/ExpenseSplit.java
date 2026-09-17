package id.patunganku.event_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Table(
        schema = "public",
        name = "expense_split",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_expense_split_expense_member",
                        columnNames = {"expense_id", "member_id"}
                )
        }
)
public class ExpenseSplit extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(
            name = "share_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal shareAmount;
}
