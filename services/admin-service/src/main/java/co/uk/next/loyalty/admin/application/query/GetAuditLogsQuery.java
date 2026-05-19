package co.uk.Dunelm.loyalty.admin.application.query;

import co.uk.Dunelm.loyalty.admin.domain.model.AuditEntry;
import co.uk.Dunelm.loyalty.admin.infrastructure.persistence.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class GetAuditLogsQuery {

    private final AuditLogRepository repository;

    public GetAuditLogsQuery(AuditLogRepository repository) {
        this.repository = repository;
    }

    public Page<AuditEntry> execute(int pageNumber, int pageSize) {
        return repository.findAll(PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
