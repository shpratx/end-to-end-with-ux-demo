package co.uk.Dunelm.loyalty.admin.api.controller;

import co.uk.Dunelm.loyalty.admin.application.command.CreateCampaignCommand;
import co.uk.Dunelm.loyalty.admin.application.command.UpdateCampaignCommand;
import co.uk.Dunelm.loyalty.admin.application.command.UpdateRatesConfigCommand;
import co.uk.Dunelm.loyalty.admin.application.dto.CampaignResponse;
import co.uk.Dunelm.loyalty.admin.application.dto.DashboardResponse;
import co.uk.Dunelm.loyalty.admin.application.dto.RatesConfigResponse;
import co.uk.Dunelm.loyalty.admin.application.query.GetAdminDashboardQuery;
import co.uk.Dunelm.loyalty.admin.application.query.GetAuditLogsQuery;
import co.uk.Dunelm.loyalty.admin.application.query.GetCampaignsQuery;
import co.uk.Dunelm.loyalty.admin.application.query.GetRatesConfigQuery;
import co.uk.Dunelm.loyalty.admin.domain.model.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final GetAdminDashboardQuery getDashboard;
    private final GetRatesConfigQuery getRatesConfig;
    private final UpdateRatesConfigCommand updateRatesConfig;
    private final GetCampaignsQuery getCampaigns;
    private final CreateCampaignCommand createCampaign;
    private final UpdateCampaignCommand updateCampaign;
    private final GetAuditLogsQuery getAuditLogs;

    public AdminController(GetAdminDashboardQuery getDashboard, GetRatesConfigQuery getRatesConfig,
                           UpdateRatesConfigCommand updateRatesConfig, GetCampaignsQuery getCampaigns,
                           CreateCampaignCommand createCampaign, UpdateCampaignCommand updateCampaign,
                           GetAuditLogsQuery getAuditLogs) {
        this.getDashboard = getDashboard;
        this.getRatesConfig = getRatesConfig;
        this.updateRatesConfig = updateRatesConfig;
        this.getCampaigns = getCampaigns;
        this.createCampaign = createCampaign;
        this.updateCampaign = updateCampaign;
        this.getAuditLogs = getAuditLogs;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return getDashboard.execute();
    }

    @GetMapping("/config/rates")
    public RatesConfigResponse configRates() {
        return getRatesConfig.execute();
    }

    @PutMapping("/config/rates")
    public RatesConfigResponse updateConfigRates(@RequestBody UpdateRatesConfigCommand.Request request) {
        return updateRatesConfig.execute(request);
    }

    @GetMapping("/campaigns")
    public Map<String, List<CampaignResponse>> campaigns(@RequestParam(required = false) String status) {
        return Map.of("data", getCampaigns.execute(status));
    }

    @PostMapping("/campaigns")
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse createCampaign(@RequestBody CreateCampaignCommand.Request request) {
        return createCampaign.execute(request);
    }

    @PutMapping("/campaigns/{id}")
    public CampaignResponse updateCampaign(@PathVariable UUID id, @RequestBody UpdateCampaignCommand.Request request) {
        return updateCampaign.execute(id, request);
    }

    @GetMapping("/audit")
    public Map<String, Object> auditLogs(@RequestParam(defaultValue = "1") int pageNumber,
                                          @RequestParam(defaultValue = "20") int pageSize) {
        Page<AuditEntry> page = getAuditLogs.execute(pageNumber, pageSize);
        return Map.of(
                "data", page.getContent(),
                "meta", Map.of(
                        "pageNumber", page.getNumber() + 1,
                        "pageSize", page.getSize(),
                        "totalItems", page.getTotalElements(),
                        "totalPages", page.getTotalPages()
                )
        );
    }
}
