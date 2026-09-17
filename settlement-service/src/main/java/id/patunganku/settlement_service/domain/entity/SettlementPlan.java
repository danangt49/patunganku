package id.patunganku.settlement_service.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(schema = "public", name = "settlement_plan")
public class SettlementPlan extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private Long generatedAt = System.currentTimeMillis();

    @OneToMany(
            mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<SettlementTransaction> transactions = new ArrayList<>();
}