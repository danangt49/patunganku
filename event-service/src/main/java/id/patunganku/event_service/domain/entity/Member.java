package id.patunganku.event_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Entity
@Table(
        schema = "public",
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_event_user",
                        columnNames = {"event_id", "keycloak_user_id"}
                )
        }
)
@Getter
@Setter
public class Member extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "keycloak_user_id", nullable = false, length = 100)
    private String keycloakUserId;
}