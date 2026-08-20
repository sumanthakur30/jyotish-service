package com.shopmanagement.jyotishservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.ClientApi.ClientResponse;
import com.shopmanagement.jyotishservice.api.ClientApi.UpsertClientRequest;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.BirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;
import com.shopmanagement.jyotishservice.persistence.repo.BirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishAppointmentRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientBirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientRepository;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

  @Mock private JyotishClientRepository clientRepository;
  @Mock private JyotishClientBirthProfileRepository linkRepository;
  @Mock private BirthProfileRepository birthProfileRepository;
  @Mock private JyotishAppointmentRepository appointmentRepository;

  private ClientService clientService;

  @BeforeEach
  void setUp() {
    clientService =
        new ClientService(
            clientRepository, linkRepository, birthProfileRepository, appointmentRepository);
    TenantContextFilter.bindTenantForTests("TENANT-A");
  }

  @AfterEach
  void tearDown() {
    TenantContextFilter.clearTenantForTests();
  }

  @Test
  void createStampsTenantAndReturnsClient() {
    when(clientRepository.save(any(JyotishClientEntity.class)))
        .thenAnswer(
            inv -> {
              JyotishClientEntity e = inv.getArgument(0);
              e.setId(10L);
              return e;
            });
    when(linkRepository.findByClientIdAndTenantIdOrderByCreatedAtAsc(10L, "TENANT-A"))
        .thenReturn(List.of());

    ClientResponse res =
        clientService.create(
            new UpsertClientRequest("Ravi", "9876543210", "ravi@example.com", "VIP", List.of()));

    ArgumentCaptor<JyotishClientEntity> captor = ArgumentCaptor.forClass(JyotishClientEntity.class);
    verify(clientRepository).save(captor.capture());
    assertEquals("TENANT-A", captor.getValue().getTenantId());
    assertEquals("Ravi", res.name());
    assertEquals("9876543210", res.mobile());
  }

  @Test
  void getRejectsOtherTenant() {
    when(clientRepository.findByIdAndTenantIdAndDeletedAtIsNull(5L, "TENANT-A"))
        .thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> clientService.get(5L));
    assertEquals(404, ex.getStatusCode().value());
    assertTrue(ex.getReason().contains("tenant"));
  }

  @Test
  void updateDoesNotTouchOtherTenantRow() {
    when(clientRepository.findByIdAndTenantIdAndDeletedAtIsNull(7L, "TENANT-A"))
        .thenReturn(Optional.empty());

    assertThrows(
        ResponseStatusException.class,
        () ->
            clientService.update(
                7L, new UpsertClientRequest("X", null, null, null, List.of())));
    verify(clientRepository, never()).save(any());
  }

  @Test
  void linksOnlyBirthProfilesOwnedByTenant() {
    JyotishClientEntity existing = new JyotishClientEntity();
    existing.setId(3L);
    existing.setTenantId("TENANT-A");
    existing.setName("Old");
    when(clientRepository.findByIdAndTenantIdAndDeletedAtIsNull(3L, "TENANT-A"))
        .thenReturn(Optional.of(existing));
    when(clientRepository.save(any(JyotishClientEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    when(birthProfileRepository.findByIdAndTenantIdAndDeletedAtIsNull(99L, "TENANT-A"))
        .thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                clientService.update(
                    3L, new UpsertClientRequest("New", null, null, null, List.of(99L))));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void searchUsesBoundTenant() {
    JyotishClientEntity row = new JyotishClientEntity();
    row.setId(1L);
    row.setTenantId("TENANT-A");
    row.setName("Meera");
    when(clientRepository.search("TENANT-A", "mee")).thenReturn(List.of(row));
    when(linkRepository.findByClientIdAndTenantIdOrderByCreatedAtAsc(1L, "TENANT-A"))
        .thenReturn(List.of());

    List<ClientResponse> items = clientService.search("mee");
    assertEquals(1, items.size());
    assertEquals("Meera", items.get(0).name());
    verify(clientRepository).search(eq("TENANT-A"), eq("mee"));
  }

  @Test
  void createLinksOwnedBirthProfile() {
    BirthProfileEntity profile = new BirthProfileEntity();
    profile.setId(2L);
    when(clientRepository.save(any(JyotishClientEntity.class)))
        .thenAnswer(
            inv -> {
              JyotishClientEntity e = inv.getArgument(0);
              e.setId(11L);
              return e;
            });
    when(birthProfileRepository.findByIdAndTenantIdAndDeletedAtIsNull(2L, "TENANT-A"))
        .thenReturn(Optional.of(profile));
    when(linkRepository.findByClientIdAndTenantIdOrderByCreatedAtAsc(11L, "TENANT-A"))
        .thenReturn(List.of());

    ClientResponse res =
        clientService.create(
            new UpsertClientRequest("Linked", null, null, null, List.of(2L)));
    assertEquals(11L, res.id());
    verify(linkRepository).save(any());
  }
}
