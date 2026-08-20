package com.shopmanagement.jyotishservice.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.shopmanagement.jyotishservice.api.AppointmentApi.AppointmentListResponse;
import com.shopmanagement.jyotishservice.api.AppointmentApi.AppointmentResponse;
import com.shopmanagement.jyotishservice.api.AppointmentApi.UpsertAppointmentRequest;
import com.shopmanagement.jyotishservice.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/jyotish/appointments")
public class AppointmentController {

  private final AppointmentService appointmentService;

  public AppointmentController(AppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AppointmentResponse create(@Valid @RequestBody UpsertAppointmentRequest body) {
    return appointmentService.create(body);
  }

  @GetMapping
  public AppointmentListResponse list(
      @RequestParam(required = false) Long clientId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @RequestParam(required = false) String status) {
    List<AppointmentResponse> items =
        appointmentService.search(clientId, fromDate, toDate, status);
    return new AppointmentListResponse(items);
  }

  @GetMapping("/{id}")
  public AppointmentResponse get(@PathVariable Long id) {
    return appointmentService.get(id);
  }

  @PutMapping("/{id}")
  public AppointmentResponse update(
      @PathVariable Long id, @Valid @RequestBody UpsertAppointmentRequest body) {
    return appointmentService.update(id, body);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    appointmentService.softDelete(id);
  }
}
