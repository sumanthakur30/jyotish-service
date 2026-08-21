package com.shopmanagement.jyotishservice.engine.ephemeris;

/** Thrown when a configured ephemeris provider cannot be initialized. */
public final class EphemerisUnavailableException extends IllegalStateException {

  public EphemerisUnavailableException(String message) {
    super(message);
  }

  public EphemerisUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
