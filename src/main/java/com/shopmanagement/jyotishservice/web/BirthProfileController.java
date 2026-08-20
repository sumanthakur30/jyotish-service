package com.shopmanagement.jyotishservice.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.jyotishservice.api.BirthProfileApi.PlaceSearchResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.ProfileListResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.ProfileResponse;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.UpsertProfileRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.WorkspaceBootstrapRequest;
import com.shopmanagement.jyotishservice.api.BirthProfileApi.WorkspaceResponse;
import com.shopmanagement.jyotishservice.service.BirthProfileService;
import com.shopmanagement.jyotishservice.service.PlaceCatalogService;
import com.shopmanagement.jyotishservice.service.WorkspaceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish")
public class BirthProfileController {

  private final BirthProfileService birthProfileService;
  private final PlaceCatalogService placeCatalogService;
  private final WorkspaceService workspaceService;

  public BirthProfileController(
      BirthProfileService birthProfileService,
      PlaceCatalogService placeCatalogService,
      WorkspaceService workspaceService) {
    this.birthProfileService = birthProfileService;
    this.placeCatalogService = placeCatalogService;
    this.workspaceService = workspaceService;
  }

  @PostMapping("/workspaces/bootstrap")
  public WorkspaceResponse bootstrap(@RequestBody(required = false) WorkspaceBootstrapRequest body) {
    return workspaceService.bootstrap(body);
  }

  @GetMapping("/workspaces/current")
  public WorkspaceResponse currentWorkspace() {
    WorkspaceResponse ws = workspaceService.getOrNull();
    if (ws == null) {
      return workspaceService.bootstrap(new WorkspaceBootstrapRequest("Sugam Jyotish"));
    }
    return ws;
  }

  @GetMapping("/places")
  public PlaceSearchResponse places(@RequestParam(required = false) String q) {
    return placeCatalogService.search(q);
  }

  @PostMapping("/profiles")
  @ResponseStatus(HttpStatus.CREATED)
  public ProfileResponse create(@Valid @RequestBody UpsertProfileRequest body) {
    return birthProfileService.create(body);
  }

  @GetMapping("/profiles")
  public ProfileListResponse list(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "false") boolean includeArchived) {
    List<ProfileResponse> items = birthProfileService.search(q, includeArchived);
    return new ProfileListResponse(items);
  }

  @GetMapping("/profiles/{id}")
  public ProfileResponse get(@PathVariable Long id) {
    return birthProfileService.get(id);
  }

  @PutMapping("/profiles/{id}")
  public ProfileResponse update(@PathVariable Long id, @Valid @RequestBody UpsertProfileRequest body) {
    return birthProfileService.update(id, body);
  }

  @PostMapping("/profiles/{id}/duplicate")
  public ProfileResponse duplicate(@PathVariable Long id) {
    return birthProfileService.duplicate(id);
  }

  @PostMapping("/profiles/{id}/archive")
  public ProfileResponse archive(@PathVariable Long id) {
    return birthProfileService.archive(id);
  }

  @DeleteMapping("/profiles/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    birthProfileService.softDelete(id);
  }
}
