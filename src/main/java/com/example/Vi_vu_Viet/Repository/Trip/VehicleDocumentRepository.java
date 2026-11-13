package com.example.Vi_vu_Viet.Repository.Trip;

import com.example.Vi_vu_Viet.Entity.Trip.VehicleDocument;
import com.example.Vi_vu_Viet.Entity.Trip.VehicleDocument.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {
    List<VehicleDocument> findByVehicleId(UUID vehicleId);
    Optional<VehicleDocument> findByVehicleIdAndDocumentType(UUID vehicleId, DocumentType documentType);
}
