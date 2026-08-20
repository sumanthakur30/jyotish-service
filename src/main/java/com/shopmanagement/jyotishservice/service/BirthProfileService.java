package com.shopmanagement.jyotishservice.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthDetailsRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthDetailsResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthLocationRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.BirthLocationResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.ProfileResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.UpsertProfileRequest;
import com.shopmanagement.jyotishservice.filter.TenantContextFilter;
import com.shopmanagement.jyotishservice.persistence.entity.BirthDetailsEntity;
import com.shopmanagement.jyotishservice.persistence.entity.BirthLocationEntity;
import com.shopmanagement.jyotishservice.persistence.entity.BirthProfileEntity;
import com.shopmanagement.jyotishservice.persistence.repo.BirthDetailsRepository;
import com.shopmanagement.jyotishservice.persistence.repo.BirthLocationRepository;
import com.shopmanagement.jyotishservice.persistence.repo.BirthProfileRepository;

@Service
public class BirthProfileService {

  private final BirthProfileRepository profileRepository;
  private final BirthDetailsRepository detailsRepository;
  private final BirthLocationRepository locationRepository;

  public BirthProfileService(
      BirthProfileRepository profileRepository,
      BirthDetailsRepository detailsRepository,
      BirthLocationRepository locationRepository) {
    this.profileRepository = profileRepository;
    this.detailsRepository = detailsRepository;
    this.locationRepository = locationRepository;
  }

  @Transactional
  public ProfileResponse create(UpsertProfileRequest request) {
    String tenantId = requireTenant();
    validateRequest(request);

    BirthProfileEntity profile = new BirthProfileEntity();
    profile.setTenantId(tenantId);
    applyProfileFields(profile, request);
    profile = profileRepository.save(profile);

    BirthDetailsEntity details = new BirthDetailsEntity();
    details.setTenantId(tenantId);
    details.setProfileId(profile.getId());
    applyDetails(details, request.details());
    detailsRepository.save(details);

    BirthLocationEntity location = new BirthLocationEntity();
    location.setTenantId(tenantId);
    location.setProfileId(profile.getId());
    applyLocation(location, request.location());
    locationRepository.save(location);

    return toResponse(profile, details, location);
  }

  @Transactional
  public ProfileResponse update(Long id, UpsertProfileRequest request) {
    String tenantId = requireTenant();
    validateRequest(request);
    BirthProfileEntity profile = requireProfile(id, tenantId);
    applyProfileFields(profile, request);
    profile = profileRepository.save(profile);

    BirthDetailsEntity details =
        detailsRepository
            .findByProfileIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth details missing"));
    applyDetails(details, request.details());
    detailsRepository.save(details);

    BirthLocationEntity location =
        locationRepository
            .findByProfileIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth location missing"));
    applyLocation(location, request.location());
    locationRepository.save(location);

    return toResponse(profile, details, location);
  }

  @Transactional(readOnly = true)
  public ProfileResponse get(Long id) {
    String tenantId = requireTenant();
    BirthProfileEntity profile = requireProfile(id, tenantId);
    return loadResponse(profile, tenantId);
  }

  @Transactional(readOnly = true)
  public List<ProfileResponse> search(String q, boolean includeArchived) {
    String tenantId = requireTenant();
    List<BirthProfileEntity> profiles = profileRepository.search(tenantId, q, includeArchived);
    List<ProfileResponse> out = new ArrayList<>(profiles.size());
    for (BirthProfileEntity profile : profiles) {
      out.add(loadResponse(profile, tenantId));
    }
    return out;
  }

  @Transactional
  public ProfileResponse duplicate(Long id) {
    String tenantId = requireTenant();
    BirthProfileEntity source = requireProfile(id, tenantId);
    BirthDetailsEntity sourceDetails =
        detailsRepository
            .findByProfileIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth details missing"));
    BirthLocationEntity sourceLocation =
        locationRepository
            .findByProfileIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth location missing"));

    BirthProfileEntity copy = new BirthProfileEntity();
    copy.setTenantId(tenantId);
    copy.setDisplayName(source.getDisplayName() + " (copy)");
    copy.setGender(source.getGender());
    copy.setNotes(source.getNotes());
    copy.setClientRef(source.getClientRef());
    copy.setStatus("ACTIVE");
    copy = profileRepository.save(copy);

    BirthDetailsEntity details = new BirthDetailsEntity();
    details.setTenantId(tenantId);
    details.setProfileId(copy.getId());
    details.setBirthDate(sourceDetails.getBirthDate());
    details.setBirthTime(sourceDetails.getBirthTime());
    details.setBirthTimeUnknown(sourceDetails.isBirthTimeUnknown());
    details.setDstObserved(sourceDetails.isDstObserved());
    details.setTimeZone(sourceDetails.getTimeZone());
    detailsRepository.save(details);

    BirthLocationEntity location = new BirthLocationEntity();
    location.setTenantId(tenantId);
    location.setProfileId(copy.getId());
    location.setPlaceName(sourceLocation.getPlaceName());
    location.setCountryCode(sourceLocation.getCountryCode());
    location.setLatitude(sourceLocation.getLatitude());
    location.setLongitude(sourceLocation.getLongitude());
    location.setTimeZone(sourceLocation.getTimeZone());
    location.setCoordsManual(sourceLocation.isCoordsManual());
    locationRepository.save(location);

    return toResponse(copy, details, location);
  }

  @Transactional
  public ProfileResponse archive(Long id) {
    String tenantId = requireTenant();
    BirthProfileEntity profile = requireProfile(id, tenantId);
    profile.setStatus("ARCHIVED");
    profile.setArchivedAt(Instant.now());
    profile = profileRepository.save(profile);
    return loadResponse(profile, tenantId);
  }

  @Transactional
  public void softDelete(Long id) {
    String tenantId = requireTenant();
    BirthProfileEntity profile = requireProfile(id, tenantId);
    profile.setDeletedAt(Instant.now());
    profile.setStatus("ARCHIVED");
    if (profile.getArchivedAt() == null) {
      profile.setArchivedAt(Instant.now());
    }
    profileRepository.save(profile);
  }

  private ProfileResponse loadResponse(BirthProfileEntity profile, String tenantId) {
    BirthDetailsEntity details =
        detailsRepository
            .findByProfileIdAndTenantId(profile.getId(), tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth details missing"));
    BirthLocationEntity location =
        locationRepository
            .findByProfileIdAndTenantId(profile.getId(), tenantId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Birth location missing"));
    return toResponse(profile, details, location);
  }

  private BirthProfileEntity requireProfile(Long id, String tenantId) {
    return profileRepository
        .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Birth profile not found for this tenant"));
  }

  private static void validateRequest(UpsertProfileRequest request) {
    if (request.details() == null) {
      throw new IllegalArgumentException("Birth details are required.");
    }
    if (request.location() == null) {
      throw new IllegalArgumentException("Please select a valid birth location.");
    }
    boolean timeUnknown =
        request.details().birthTimeUnknown() != null && request.details().birthTimeUnknown();
    if (!timeUnknown && request.details().birthTime() == null) {
      throw new IllegalArgumentException(
          "Birth time is required for accurate Lagna and house calculations.");
    }
  }

  private static void applyProfileFields(BirthProfileEntity profile, UpsertProfileRequest request) {
    profile.setDisplayName(request.displayName().trim());
    profile.setGender(blankToNull(request.gender()));
    profile.setClientRef(blankToNull(request.clientRef()));
    profile.setNotes(request.notes());
  }

  private static void applyDetails(BirthDetailsEntity details, BirthDetailsRequest request) {
    details.setBirthDate(request.birthDate());
    boolean timeUnknown = request.birthTimeUnknown() != null && request.birthTimeUnknown();
    details.setBirthTimeUnknown(timeUnknown);
    details.setBirthTime(timeUnknown ? null : request.birthTime());
    details.setDstObserved(request.dstObserved() != null && request.dstObserved());
    details.setTimeZone(request.timeZone().trim());
  }

  private static void applyLocation(BirthLocationEntity location, BirthLocationRequest request) {
    location.setPlaceName(request.placeName().trim());
    location.setCountryCode(blankToNull(request.countryCode()));
    location.setLatitude(request.latitude());
    location.setLongitude(request.longitude());
    location.setTimeZone(request.timeZone().trim());
    location.setCoordsManual(request.coordsManual() != null && request.coordsManual());
  }

  private static ProfileResponse toResponse(
      BirthProfileEntity profile, BirthDetailsEntity details, BirthLocationEntity location) {
    return new ProfileResponse(
        profile.getId(),
        profile.getDisplayName(),
        profile.getGender(),
        profile.getStatus(),
        profile.getClientRef(),
        profile.getNotes(),
        new BirthDetailsResponse(
            details.getBirthDate(),
            details.getBirthTime(),
            details.isBirthTimeUnknown(),
            details.isDstObserved(),
            details.getTimeZone()),
        new BirthLocationResponse(
            location.getPlaceName(),
            location.getCountryCode(),
            location.getLatitude(),
            location.getLongitude(),
            location.getTimeZone(),
            location.isCoordsManual()),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
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
