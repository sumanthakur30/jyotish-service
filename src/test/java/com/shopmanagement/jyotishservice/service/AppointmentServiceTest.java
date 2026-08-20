package com.shopmanagement.jyotishservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.AppointmentApi.AppointmentResponse;
import com.shopmanagement.jyotishservice.api.AppointmentApi.UpsertAppointmentRequest;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishAppointmentEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishAppointmentRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

  @Mock private JyotishAppointmentRepository appointmentRepository;
  @Mock private ClientService clientService;

  private AppointmentService appointmentService;

  @BeforeEach
  void setUp() {
    appointmentService = new AppointmentService(appointmentRepository, clientService);
    TenantContextFilter.bindTenantForTests("TENANT-A");
  }

  @AfterEach
  void tearDown() {
    TenantContextFilter.clearTenantForTests();
  }

  @Test
  void createPersistsTenantScopedAppointment() {
    JyotishClientEntity client = new JyotishClientEntity();
    client.setId(4L);
    client.setTenantId("TENANT-A");
    client.setName("Anita");
    when(clientService.requireClient(4L, "TENANT-A")).thenReturn(client);
    when(appointmentRepository.save(any(JyotishAppointmentEntity.class)))
        .thenAnswer(
            inv -> {
              JyotishAppointmentEntity e = inv.getArgument(0);
              e.setId(20L);
              return e;
            });

    AppointmentResponse res =
        appointmentService.create(
            new UpsertAppointmentRequest(
                4L,
                LocalDate.of(2026, 8, 21),
                LocalTime.of(10, 30),
                "KUNDALI_READING",
                "CONFIRMED",
                "UNPAID",
                "First visit"));

    ArgumentCaptor<JyotishAppointmentEntity> captor =
        ArgumentCaptor.forClass(JyotishAppointmentEntity.class);
    verify(appointmentRepository).save(captor.capture());
    JyotishAppointmentEntity saved = captor.getValue();
    assertEquals("TENANT-A", saved.getTenantId());
    assertEquals(4L, saved.getClientId());
    assertEquals("CONFIRMED", saved.getStatus());
    assertEquals("Anita", res.clientName());
    assertEquals(20L, res.id());
  }

  @Test
  void createFailsWhenClientMissingForTenant() {
    when(clientService.requireClient(99L, "TENANT-A"))
        .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "missing"));

    assertThrows(
        ResponseStatusException.class,
        () ->
            appointmentService.create(
                new UpsertAppointmentRequest(
                    99L,
                    LocalDate.of(2026, 8, 21),
                    LocalTime.of(11, 0),
                    "MATCHING",
                    null,
                    null,
                    null)));
  }

  @Test
  void getRejectsOtherTenantAppointment() {
    when(appointmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(8L, "TENANT-A"))
        .thenReturn(Optional.empty());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> appointmentService.get(8L));
    assertEquals(404, ex.getStatusCode().value());
  }
}
