package com.example.Vi_vu_Viet.Entity.Trip;

import com.example.Vi_vu_Viet.Entity.Common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_documents", uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "document_type"}))
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleDocument extends BaseEntity {
    public enum DocumentType { REGISTRATION, INSPECTION, LIABILITY_INSURANCE, PHYSICAL_DAMAGE_INSURANCE }
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private DocumentType documentType;
    @Column(length = 120) private String documentNumber;
    @Column(length = 120) private String issuer;
    @Column(length = 255) private String description;
    private LocalDate issuedOn;
    private LocalDate expiresOn;
}
