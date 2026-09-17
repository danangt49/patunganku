package id.patunganku.settlement_service.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Base {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected Long createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected Long updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    protected String updatedBy;
}