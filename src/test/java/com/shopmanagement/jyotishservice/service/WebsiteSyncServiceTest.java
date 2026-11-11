package com.shopmanagement.jyotishservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.jyotishservice.api.AppointmentApi.AppointmentResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthDetailsResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthLocationResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSearchResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSuggestion;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.ProfileResponse;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;
import com.shopmanagement.jyotishservice.persistence.entity.WebsiteSyncIdempotencyEntity;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientBirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientRepository;
import com.shopmanagement.jyotishservice.persistence.repo.WebsiteSyncExternalIdRepository;
import com.shopmanagement.jyotishservice.persistence.repo.WebsiteSyncIdempotencyRepository;

@ExtendWith(MockitoExtension.class)
class WebsiteSyncServiceTest {

  @Mock private JyotishClientRepository clients;
  @Mock private JyotishClientBirthProfileRepository links;
  @Mock private WebsiteSyncExternalIdRepository externalIds;
  @Mock private WebsiteSyncIdempotencyRepository idempotency;
  @Mock private BirthProfileService birthProfiles;
  @Mock private AppointmentService appointments;
  @Mock private PlaceCatalogService places;

  private WebsiteSyncService sync;

  @BeforeEach
  void setUp() {
    sync =
        new WebsiteSyncService(
            clients,
            links,
            externalIds,
            idempotency,
            birthProfiles,
            appointments,
            places,
            new ObjectMapper());
    TenantContextFilter.bindTenant("8801");
  }

  @AfterEach
  void tearDown() {
    TenantContextFilter.clearTenantForTests();
  }

  @Test
  void duplicateMobileDoesNotCreateSecondCustomer() {
    JyotishClientEntity existing = new JyotishClientEntity();
    existing.setId(42L);
    existing.setTenantId("8801");
    existing.setName("Rahul Sharma");
    existing.setMobile("9876543210");
    when(clients.findFirstByTenantIdAndMobileAndDeletedAtIsNull("8801", "9876543210"))
        .thenReturn(Optional.of(existing));
    when(clients.save(any(JyotishClientEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    when(externalIds.findByTenantIdAndEntityTypeAndExternalId(eq("8801"), eq("CUSTOMER"), any()))
        .thenReturn(Optional.empty());

    Map<String, Object> first =
        sync.upsertCustomer(
            Map.of(
                "name",
                "Rahul Sharma",
                "mobile",
                "9876543210",
                "external_customer_id",
                "JHA-CUST-10025"),
            "evt_1");
    Map<String, Object> second =
        sync.upsertCustomer(Map.of("name", "Rahul Sharma", "mobile", "9876543210"), "evt_2");

    assertEquals(first.get("customer_id"), second.get("customer_id"));
    assertEquals("SF-CUST-42", second.get("customer_id"));
    assertFalse(Boolean.TRUE.equals(second.get("created")));
  }

  @Test
  void idempotencyKeyReplaysSameBody() {
    WebsiteSyncIdempotencyEntity row = new WebsiteSyncIdempotencyEntity();
    row.setResponseJson("{\"success\":true,\"customer_id\":\"SF-CUST-7\"}");
    when(idempotency.findByTenantIdAndIdempotencyKey("8801", "evt_same"))
        .thenReturn(Optional.of(row));

    Map<String, Object> a = sync.upsertCustomer(Map.of("name", "Idem"), "evt_same");
    Map<String, Object> b = sync.upsertCustomer(Map.of("name", "Idem"), "evt_same");
    assertEquals(a, b);
    assertEquals("SF-CUST-7", a.get("customer_id"));
  }

  @Test
  void kundliCreatesBirthProfileUnderClient() {
    when(clients.save(any(JyotishClientEntity.class)))
        .thenAnswer(
            inv -> {
              JyotishClientEntity e = inv.getArgument(0);
              e.setId(5L);
              return e;
            });
    when(externalIds.findByTenantIdAndEntityTypeAndExternalId(eq("8801"), any(), any()))
        .thenReturn(Optional.empty());
    when(places.search("Delhi"))
        .thenReturn(
            new PlaceSearchResponse(
                List.of(
                    new PlaceSuggestion(
                        "Delhi, India",
                        "IN",
                        new java.math.BigDecimal("28.6139"),
                        new java.math.BigDecimal("77.2090"),
                        "Asia/Kolkata"))));
    when(birthProfiles.create(any()))
        .thenReturn(
            new ProfileResponse(
                99L,
                "Rahul Sharma",
                null,
                "ACTIVE",
                "JHA-KUNDLI-30001",
                "jhaastro.com",
                new BirthDetailsResponse(
                    LocalDate.of(1995, 8, 15), null, true, "UNKNOWN", null, false, "Asia/Kolkata"),
                new BirthLocationResponse(
                    "Delhi, India",
                    "IN",
                    new java.math.BigDecimal("28.6139"),
                    new java.math.BigDecimal("77.2090"),
                    "Asia/Kolkata",
                    true),
                null,
                null));
    when(links.existsByClientIdAndBirthProfileIdAndTenantId(5L, 99L, "8801")).thenReturn(false);

    Map<String, Object> res =
        sync.upsertKundli(
            Map.of(
                "name",
                "Rahul Sharma",
                "mobile",
                "9111111111",
                "date_of_birth",
                "1995-08-15",
                "time_of_birth",
                "10:30:00",
                "birth_place",
                "Delhi",
                "external_kundli_id",
                "JHA-KUNDLI-30001"),
            "evt_k1");

    assertEquals(true, res.get("success"));
    assertEquals("SF-KUNDLI-99", res.get("kundli_id"));
    assertEquals("SF-PERSON-99", res.get("person_id"));
    ArgumentCaptor<com.shopmanagement.jyotishservice.api.BirthProfileApi.UpsertProfileRequest>
        captor =
            ArgumentCaptor.forClass(
                com.shopmanagement.jyotishservice.api.BirthProfileApi.UpsertProfileRequest.class);
    verify(birthProfiles).create(captor.capture());
    assertEquals(LocalDate.of(1995, 8, 15), captor.getValue().details().birthDate());
  }

  @Test
  void bookingCreatesAppointment() {
    when(clients.save(any(JyotishClientEntity.class)))
        .thenAnswer(
            inv -> {
              JyotishClientEntity e = inv.getArgument(0);
              e.setId(8L);
              return e;
            });
    when(externalIds.findByTenantIdAndEntityTypeAndExternalId(eq("8801"), any(), any()))
        .thenReturn(Optional.empty());
    when(places.search(any()))
        .thenReturn(new PlaceSearchResponse(List.of()));
    when(birthProfiles.create(any()))
        .thenReturn(
            new ProfileResponse(
                3L,
                "Booker",
                null,
                "ACTIVE",
                null,
                null,
                new BirthDetailsResponse(
                    LocalDate.of(2000, 1, 1), null, true, "UNKNOWN", null, false, "Asia/Kolkata"),
                new BirthLocationResponse(
                    "Delhi, India",
                    "IN",
                    new java.math.BigDecimal("28.61"),
                    new java.math.BigDecimal("77.20"),
                    "Asia/Kolkata",
                    true),
                null,
                null));
    when(links.existsByClientIdAndBirthProfileIdAndTenantId(8L, 3L, "8801")).thenReturn(false);
    when(appointments.create(any()))
        .thenReturn(
            new AppointmentResponse(
                77L,
                8L,
                "Booker",
                LocalDate.of(2026, 9, 2),
                java.time.LocalTime.of(11, 0),
                "Kundli",
                "SCHEDULED",
                "PENDING",
                null,
                null,
                null));

    Map<String, Object> res =
        sync.upsertBooking(
            Map.of(
                "name",
                "Booker",
                "mobile",
                "9000000099",
                "service_name",
                "Kundli",
                "booking_date",
                "2026-09-02",
                "booking_time",
                "11:00",
                "external_booking_id",
                "JHA-BOOK-1"),
            "evt_b1");

    assertEquals("SF-BOOK-77", res.get("booking_id"));
    verify(appointments).create(any());
  }
}
