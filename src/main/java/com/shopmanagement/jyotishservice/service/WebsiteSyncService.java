package com.shopmanagement.jyotishservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopmanagement.jyotishservice.api.AppointmentApi.AppointmentResponse;
import com.shopmanagement.jyotishservice.api.AppointmentApi.UpsertAppointmentRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthDetailsRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthLocationRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSuggestion;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.ProfileResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.UpsertProfileRequest;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientBirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.entity.JyotishClientEntity;
import com.shopmanagement.jyotishservice.persistence.entity.WebsiteSyncExternalIdEntity;
import com.shopmanagement.jyotishservice.persistence.entity.WebsiteSyncIdempotencyEntity;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientBirthProfileRepository;
import com.shopmanagement.jyotishservice.persistence.repo.JyotishClientRepository;
import com.shopmanagement.jyotishservice.persistence.repo.WebsiteSyncExternalIdRepository;
import com.shopmanagement.jyotishservice.persistence.repo.WebsiteSyncIdempotencyRepository;

/**
 * Maps JhaAstro outbox events onto CRM clients, birth profiles, and appointments.
 */
@Service
public class WebsiteSyncService {

  private static final String CUSTOMER = "CUSTOMER";
  private static final String KUNDLI = "KUNDLI";
  private static final String BOOKING = "BOOKING";
  private static final BigDecimal DEFAULT_LAT = new BigDecimal("28.6139000");
  private static final BigDecimal DEFAULT_LON = new BigDecimal("77.2090000");
  private static final String DEFAULT_TZ = "Asia/Kolkata";

  private final JyotishClientRepository clients;
  private final JyotishClientBirthProfileRepository links;
  private final WebsiteSyncExternalIdRepository externalIds;
  private final WebsiteSyncIdempotencyRepository idempotency;
  private final BirthProfileService birthProfiles;
  private final AppointmentService appointments;
  private final PlaceCatalogService places;
  private final ObjectMapper mapper;

  public WebsiteSyncService(
      JyotishClientRepository clients,
      JyotishClientBirthProfileRepository links,
      WebsiteSyncExternalIdRepository externalIds,
      WebsiteSyncIdempotencyRepository idempotency,
      BirthProfileService birthProfiles,
      AppointmentService appointments,
      PlaceCatalogService places,
      ObjectMapper mapper) {
    this.clients = clients;
    this.links = links;
    this.externalIds = externalIds;
    this.idempotency = idempotency;
    this.birthProfiles = birthProfiles;
    this.appointments = appointments;
    this.places = places;
    this.mapper = mapper;
  }

  @Transactional
  public Map<String, Object> upsertCustomer(Map<String, Object> body, String idemKey) {
    String tenantId = requireTenant();
    Optional<Map<String, Object>> cached = replay(tenantId, idemKey);
    if (cached.isPresent()) {
      return cached.get();
    }
    CustomerUpsert upsert = upsertCustomerInternal(tenantId, body);
    Map<String, Object> res =
        result(
            Map.of(
                "customer_id", publicId("SF-CUST", upsert.client().getId()),
                "external_id", blankToEmpty(first(str(body, "external_customer_id"), str(body, "external_id"))),
                "created", upsert.created()));
    storeIdem(tenantId, idemKey, res);
    return res;
  }

  @Transactional
  public Map<String, Object> upsertKundli(Map<String, Object> body, String idemKey) {
    String tenantId = requireTenant();
    Optional<Map<String, Object>> cached = replay(tenantId, idemKey);
    if (cached.isPresent()) {
      return cached.get();
    }
    CustomerUpsert upsert = upsertCustomerInternal(tenantId, body);
    ProfileResponse profile = upsertBirthProfile(tenantId, upsert.client(), body);
    Map<String, Object> res =
        result(
            Map.of(
                "customer_id", publicId("SF-CUST", upsert.client().getId()),
                "person_id", publicId("SF-PERSON", profile.id()),
                "kundli_id", publicId("SF-KUNDLI", profile.id())));
    storeIdem(tenantId, idemKey, res);
    return res;
  }

  @Transactional
  public Map<String, Object> upsertBooking(Map<String, Object> body, String idemKey) {
    String tenantId = requireTenant();
    Optional<Map<String, Object>> cached = replay(tenantId, idemKey);
    if (cached.isPresent()) {
      return cached.get();
    }
    CustomerUpsert upsert = upsertCustomerInternal(tenantId, body);
    ProfileResponse profile = upsertBirthProfile(tenantId, upsert.client(), body);
    String bookExt = first(str(body, "external_booking_id"), str(body, "external_id"));
    Long existingBooking = lookupInternal(tenantId, BOOKING, bookExt);
    String type =
        first(
            str(body, "service_name"),
            str(body, "serviceName"),
            str(body, "service_id"),
            str(body, "consultationType"),
            "Consultation");
    if (type.length() > 64) {
      type = type.substring(0, 64);
    }
    LocalDate date = parseDate(first(str(body, "booking_date"), str(body, "date")));
    if (date == null) {
      date = LocalDate.now();
    }
    LocalTime time = parseTime(first(str(body, "booking_time"), str(body, "time")));
    if (time == null) {
      time = LocalTime.of(10, 0);
    }
    String payment = first(str(body, "payment_status"), str(body, "paymentStatus"), "PENDING");
    payment = normalizePayment(payment);
    String notes = first(str(body, "concern"), str(body, "notes"));
    AppointmentResponse appt;
    UpsertAppointmentRequest request =
        new UpsertAppointmentRequest(
            upsert.client().getId(), date, time, type, "SCHEDULED", payment, notes);
    if (existingBooking != null) {
      appt = appointments.update(existingBooking, request);
    } else {
      appt = appointments.create(request);
      rememberExternal(tenantId, BOOKING, bookExt, appt.id());
    }
    Map<String, Object> res =
        result(
            Map.of(
                "customer_id", publicId("SF-CUST", upsert.client().getId()),
                "person_id", publicId("SF-PERSON", profile.id()),
                "kundli_id", publicId("SF-KUNDLI", profile.id()),
                "booking_id", publicId("SF-BOOK", appt.id()),
                "consultation_id", publicId("SF-CONS", appt.id())));
    storeIdem(tenantId, idemKey, res);
    return res;
  }

  private CustomerUpsert upsertCustomerInternal(String tenantId, Map<String, Object> body) {
    String mobile = str(body, "mobile");
    String email = str(body, "email");
    String externalId = first(str(body, "external_customer_id"), str(body, "external_id"));
    JyotishClientEntity existing = findCustomer(tenantId, externalId, mobile, email);
    boolean created = existing == null;
    if (existing == null) {
      existing = new JyotishClientEntity();
      existing.setTenantId(tenantId);
      existing.setName(first(str(body, "name"), "Customer"));
    } else if (str(body, "name") != null) {
      existing.setName(str(body, "name"));
    }
    if (mobile != null) {
      existing.setMobile(mobile);
    }
    if (email != null) {
      existing.setEmail(email);
    }
    String noteBits = customerNotes(body);
    if (noteBits != null) {
      existing.setNotes(mergeNotes(existing.getNotes(), noteBits));
    }
    existing = clients.save(existing);
    rememberExternal(tenantId, CUSTOMER, externalId, existing.getId());
    return new CustomerUpsert(existing, created);
  }

  private ProfileResponse upsertBirthProfile(
      String tenantId, JyotishClientEntity client, Map<String, Object> body) {
    LocalDate dob = parseDate(first(str(body, "date_of_birth"), str(body, "dob")));
    if (dob == null) {
      dob = LocalDate.of(2000, 1, 1);
    }
    LocalTime tob = parseTime(first(str(body, "time_of_birth"), str(body, "birthTime")));
    String placeName = first(str(body, "birth_place"), str(body, "birthPlace"), "Delhi, India");
    PlaceSuggestion place = resolvePlace(placeName, body);
    String kundliExt = first(str(body, "external_kundli_id"), str(body, "external_person_id"));
    Long existingProfile = lookupInternal(tenantId, KUNDLI, kundliExt);
    boolean timeUnknown = tob == null;
    UpsertProfileRequest request =
        new UpsertProfileRequest(
            first(str(body, "name"), client.getName()),
            str(body, "gender"),
            kundliExt,
            first(str(body, "source"), "jhaastro.com"),
            new BirthDetailsRequest(
                dob,
                tob,
                timeUnknown,
                timeUnknown ? "UNKNOWN" : "EXACT",
                null,
                false,
                place.timeZone()),
            new BirthLocationRequest(
                place.placeName(),
                place.countryCode(),
                place.latitude(),
                place.longitude(),
                place.timeZone(),
                body.get("latitude") == null));
    ProfileResponse profile =
        existingProfile == null
            ? birthProfiles.create(request)
            : birthProfiles.update(existingProfile, request);
    if (existingProfile == null) {
      rememberExternal(tenantId, KUNDLI, kundliExt, profile.id());
    }
    if (!links.existsByClientIdAndBirthProfileIdAndTenantId(client.getId(), profile.id(), tenantId)) {
      JyotishClientBirthProfileEntity link = new JyotishClientBirthProfileEntity();
      link.setTenantId(tenantId);
      link.setClientId(client.getId());
      link.setBirthProfileId(profile.id());
      links.save(link);
    }
    return profile;
  }

  private PlaceSuggestion resolvePlace(String placeName, Map<String, Object> body) {
    BigDecimal lat = decimal(body, "latitude");
    BigDecimal lon = decimal(body, "longitude");
    String tz = first(str(body, "timezone"), DEFAULT_TZ);
    if (lat != null && lon != null) {
      return new PlaceSuggestion(placeName, "IN", lat, lon, tz);
    }
    List<PlaceSuggestion> hits = places.search(placeName).items();
    if (!hits.isEmpty()) {
      return hits.get(0);
    }
    return new PlaceSuggestion(placeName, "IN", DEFAULT_LAT, DEFAULT_LON, tz);
  }

  private JyotishClientEntity findCustomer(
      String tenantId, String externalId, String mobile, String email) {
    Long mapped = lookupInternal(tenantId, CUSTOMER, externalId);
    if (mapped != null) {
      Optional<JyotishClientEntity> byId = clients.findByIdAndTenantIdAndDeletedAtIsNull(mapped, tenantId);
      if (byId.isPresent()) {
        return byId.get();
      }
    }
    if (mobile != null) {
      Optional<JyotishClientEntity> byMobile =
          clients.findFirstByTenantIdAndMobileAndDeletedAtIsNull(tenantId, mobile);
      if (byMobile.isPresent()) {
        return byMobile.get();
      }
    }
    if (email != null) {
      return clients
          .findFirstByTenantIdAndEmailIgnoreCaseAndDeletedAtIsNull(tenantId, email)
          .orElse(null);
    }
    return null;
  }

  private Long lookupInternal(String tenantId, String type, String externalId) {
    if (externalId == null) {
      return null;
    }
    return externalIds
        .findByTenantIdAndEntityTypeAndExternalId(tenantId, type, externalId)
        .map(WebsiteSyncExternalIdEntity::getInternalId)
        .orElse(null);
  }

  private void rememberExternal(String tenantId, String type, String externalId, Long internalId) {
    if (externalId == null || internalId == null) {
      return;
    }
    if (externalIds.findByTenantIdAndEntityTypeAndExternalId(tenantId, type, externalId).isPresent()) {
      return;
    }
    WebsiteSyncExternalIdEntity row = new WebsiteSyncExternalIdEntity();
    row.setTenantId(tenantId);
    row.setEntityType(type);
    row.setExternalId(externalId);
    row.setInternalId(internalId);
    externalIds.save(row);
  }

  private Optional<Map<String, Object>> replay(String tenantId, String key) {
    if (key == null || key.isBlank()) {
      return Optional.empty();
    }
    return idempotency
        .findByTenantIdAndIdempotencyKey(tenantId, key.trim())
        .map(e -> read(e.getResponseJson()));
  }

  private void storeIdem(String tenantId, String key, Map<String, Object> res) {
    if (key == null || key.isBlank()) {
      return;
    }
    if (idempotency.findByTenantIdAndIdempotencyKey(tenantId, key.trim()).isPresent()) {
      return;
    }
    WebsiteSyncIdempotencyEntity row = new WebsiteSyncIdempotencyEntity();
    row.setTenantId(tenantId);
    row.setIdempotencyKey(key.trim());
    row.setResponseJson(write(res));
    idempotency.save(row);
  }

  private String write(Object o) {
    try {
      return mapper.writeValueAsString(o);
    } catch (Exception e) {
      return "{}";
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> read(String json) {
    try {
      return mapper.readValue(json, Map.class);
    } catch (Exception e) {
      return Map.of("success", true);
    }
  }

  private static Map<String, Object> result(Map<String, Object> data) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("success", true);
    out.putAll(data);
    return out;
  }

  private static String requireTenant() {
    String tenantId = TenantContextFilter.getCurrentTenantId();
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing tenant context");
    }
    return tenantId;
  }

  private static String customerNotes(Map<String, Object> body) {
    String dob = first(str(body, "date_of_birth"), str(body, "dob"));
    String place = first(str(body, "birth_place"), str(body, "birthPlace"));
    if (dob == null && place == null) {
      return str(body, "notes");
    }
    StringBuilder sb = new StringBuilder("Website sync");
    if (dob != null) {
      sb.append(" | DOB ").append(dob);
    }
    if (place != null) {
      sb.append(" | ").append(place);
    }
    return sb.toString();
  }

  private static String mergeNotes(String existing, String incoming) {
    if (existing == null || existing.isBlank()) {
      return incoming;
    }
    if (existing.contains(incoming)) {
      return existing;
    }
    return existing + "\n" + incoming;
  }

  private static String normalizePayment(String payment) {
    if (payment == null) {
      return "PENDING";
    }
    String p = payment.trim().toUpperCase(Locale.ROOT);
    return switch (p) {
      case "PAID", "UNPAID", "PENDING", "WAIVED" -> p;
      case "SUCCESS", "COMPLETE", "COMPLETED" -> "PAID";
      default -> "PENDING";
    };
  }

  private static String publicId(String prefix, Long id) {
    return prefix + "-" + id;
  }

  private static String blankToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String str(Map<String, Object> body, String key) {
    Object v = body.get(key);
    if (v == null) {
      return null;
    }
    String s = String.valueOf(v).trim();
    return s.isEmpty() ? null : s;
  }

  private static String first(String... values) {
    for (String v : values) {
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return null;
  }

  private static LocalDate parseDate(String raw) {
    if (raw == null) {
      return null;
    }
    try {
      return LocalDate.parse(raw.length() >= 10 ? raw.substring(0, 10) : raw);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static LocalTime parseTime(String raw) {
    if (raw == null) {
      return null;
    }
    String t = raw.length() >= 8 ? raw.substring(0, 8) : raw;
    try {
      return LocalTime.parse(t.length() == 5 ? t + ":00" : t);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static BigDecimal decimal(Map<String, Object> body, String key) {
    Object v = body.get(key);
    if (v instanceof Number n) {
      return new BigDecimal(n.toString());
    }
    if (v instanceof String s && !s.isBlank()) {
      try {
        return new BigDecimal(s.trim());
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  private record CustomerUpsert(JyotishClientEntity client, boolean created) {}
}
