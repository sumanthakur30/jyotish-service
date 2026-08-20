package com.shopmanagement.jyotishservice.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.ClientApi.ClientResponse;
import com.shopmanagement.jyotishservice.api.ClientApi.CrmDashboardResponse;
import com.shopmanagement.jyotishservice.api.ClientApi.UpsertClientRequest;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientBirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;
import com.shopmanagement.jyotishservice.persistence.repo.BirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishAppointmentRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientBirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientRepository;

@Service
public class ClientService {

  private final JyotishClientRepository clientRepository;
  private final JyotishClientBirthProfileRepository linkRepository;
  private final BirthProfileRepository birthProfileRepository;
  private final JyotishAppointmentRepository appointmentRepository;

  public ClientService(
      JyotishClientRepository clientRepository,
      JyotishClientBirthProfileRepository linkRepository,
      BirthProfileRepository birthProfileRepository,
      JyotishAppointmentRepository appointmentRepository) {
    this.clientRepository = clientRepository;
    this.linkRepository = linkRepository;
    this.birthProfileRepository = birthProfileRepository;
    this.appointmentRepository = appointmentRepository;
  }

  @Transactional
  public ClientResponse create(UpsertClientRequest request) {
    String tenantId = requireTenant();
    JyotishClientEntity client = new JyotishClientEntity();
    client.setTenantId(tenantId);
    applyFields(client, request);
    client = clientRepository.save(client);
    replaceLinks(client.getId(), tenantId, request.birthProfileIds());
    return toResponse(client, tenantId);
  }

  @Transactional
  public ClientResponse update(Long id, UpsertClientRequest request) {
    String tenantId = requireTenant();
    JyotishClientEntity client = requireClient(id, tenantId);
    applyFields(client, request);
    client = clientRepository.save(client);
    replaceLinks(client.getId(), tenantId, request.birthProfileIds());
    return toResponse(client, tenantId);
  }

  @Transactional(readOnly = true)
  public ClientResponse get(Long id) {
    String tenantId = requireTenant();
    return toResponse(requireClient(id, tenantId), tenantId);
  }

  @Transactional(readOnly = true)
  public List<ClientResponse> search(String q) {
    String tenantId = requireTenant();
    List<JyotishClientEntity> clients = clientRepository.search(tenantId, q);
    List<ClientResponse> out = new ArrayList<>(clients.size());
    for (JyotishClientEntity client : clients) {
      out.add(toResponse(client, tenantId));
    }
    return out;
  }

  @Transactional
  public void softDelete(Long id) {
    String tenantId = requireTenant();
    JyotishClientEntity client = requireClient(id, tenantId);
    client.setDeletedAt(Instant.now());
    clientRepository.save(client);
  }

  @Transactional(readOnly = true)
  public CrmDashboardResponse dashboard() {
    String tenantId = requireTenant();
    long totalClients = clientRepository.countByTenantIdAndDeletedAtIsNull(tenantId);
    long todays =
        appointmentRepository.countByTenantIdAndAppointmentDateAndDeletedAtIsNull(
            tenantId, LocalDate.now());
    return new CrmDashboardResponse(totalClients, todays);
  }

  JyotishClientEntity requireClient(Long id, String tenantId) {
    return clientRepository
        .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Client not found for this tenant"));
  }

  private void replaceLinks(Long clientId, String tenantId, List<Long> birthProfileIds) {
    linkRepository.deleteByClientIdAndTenantId(clientId, tenantId);
    if (birthProfileIds == null || birthProfileIds.isEmpty()) {
      return;
    }
    Set<Long> unique = new LinkedHashSet<>(birthProfileIds);
    for (Long profileId : unique) {
      if (profileId == null) {
        continue;
      }
      birthProfileRepository
          .findByIdAndTenantIdAndDeletedAtIsNull(profileId, tenantId)
          .orElseThrow(
              () ->
                  new ResponseStatusException(
                      HttpStatus.BAD_REQUEST,
                      "Birth profile " + profileId + " not found for this tenant"));
      JyotishClientBirthProfileEntity link = new JyotishClientBirthProfileEntity();
      link.setTenantId(tenantId);
      link.setClientId(clientId);
      link.setBirthProfileId(profileId);
      linkRepository.save(link);
    }
  }

  private ClientResponse toResponse(JyotishClientEntity client, String tenantId) {
    List<Long> profileIds =
        linkRepository.findByClientIdAndTenantIdOrderByCreatedAtAsc(client.getId(), tenantId).stream()
            .map(JyotishClientBirthProfileEntity::getBirthProfileId)
            .toList();
    return new ClientResponse(
        client.getId(),
        client.getName(),
        client.getMobile(),
        client.getEmail(),
        client.getNotes(),
        profileIds,
        client.getCreatedAt(),
        client.getUpdatedAt());
  }

  private static void applyFields(JyotishClientEntity client, UpsertClientRequest request) {
    client.setName(request.name().trim());
    client.setMobile(blankToNull(request.mobile()));
    client.setEmail(blankToNull(request.email()));
    client.setNotes(request.notes());
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Missing tenant context header: X-Tenant-Id");
    }
    return tenantId;
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
