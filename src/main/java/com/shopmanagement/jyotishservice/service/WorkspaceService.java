package com.shopmanagement.jyotishservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.jyotishservice.api.BirthProfileApi.WorkspaceBootstrapRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.WorkspaceResponse;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishWorkspaceEntity;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishWorkspaceRepository;

@Service
public class WorkspaceService {

  private final JyotishWorkspaceRepository workspaceRepository;

  public WorkspaceService(JyotishWorkspaceRepository workspaceRepository) {
    this.workspaceRepository = workspaceRepository;
  }

  @Transactional
  public WorkspaceResponse bootstrap(WorkspaceBootstrapRequest request) {
    String tenantId = requireTenant();
    JyotishWorkspaceEntity existing =
        workspaceRepository.findByTenantIdAndDeletedAtIsNull(tenantId).orElse(null);
    if (existing != null) {
      return toResponse(existing);
    }
    JyotishWorkspaceEntity ws = new JyotishWorkspaceEntity();
    ws.setTenantId(tenantId);
    String name =
        request != null && request.name() != null && !request.name().isBlank()
            ? request.name().trim()
            : "Sugam Jyotish";
    ws.setName(name);
    return toResponse(workspaceRepository.save(ws));
  }

  @Transactional(readOnly = true)
  public WorkspaceResponse getOrNull() {
    String tenantId = requireTenant();
    return workspaceRepository
        .findByTenantIdAndDeletedAtIsNull(tenantId)
        .map(this::toResponse)
        .orElse(null);
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Missing tenant context header: X-Tenant-Id");
    }
    return tenantId;
  }

  private WorkspaceResponse toResponse(JyotishWorkspaceEntity ws) {
    return new WorkspaceResponse(
        ws.getId(),
        ws.getTenantId(),
        ws.getName(),
        ws.getTimezone(),
        ws.getLocale(),
        ws.getAyanamsaCode(),
        ws.getZodiacSystem(),
        ws.getChartStyle());
  }
}
