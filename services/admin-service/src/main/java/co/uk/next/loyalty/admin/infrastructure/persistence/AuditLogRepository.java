package co.uk.Dunelm.loyalty.admin.infrastructure.persistence;

import co.uk.Dunelm.loyalty.admin.domain.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditEntry, UUID> {
}
