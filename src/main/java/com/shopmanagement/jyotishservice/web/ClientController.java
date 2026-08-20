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

import com.shopmanagement.jyotishservice.api.ClientApi.ClientListResponse;
import com.shopmanagement.jyotishservice.api.ClientApi.ClientResponse;
import com.shopmanagement.jyotishservice.api.ClientApi.CrmDashboardResponse;
import com.shopmanagement.jyotishservice.api.ClientApi.UpsertClientRequest;
import com.shopmanagement.jyotishservice.service.ClientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/clients")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @GetMapping("/dashboard")
  public CrmDashboardResponse dashboard() {
    return clientService.dashboard();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ClientResponse create(@Valid @RequestBody UpsertClientRequest body) {
    return clientService.create(body);
  }

  @GetMapping
  public ClientListResponse list(@RequestParam(required = false) String q) {
    List<ClientResponse> items = clientService.search(q);
    return new ClientListResponse(items);
  }

  @GetMapping("/{id}")
  public ClientResponse get(@PathVariable Long id) {
    return clientService.get(id);
  }

  @PutMapping("/{id}")
  public ClientResponse update(@PathVariable Long id, @Valid @RequestBody UpsertClientRequest body) {
    return clientService.update(id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    clientService.softDelete(id);
  }
}
