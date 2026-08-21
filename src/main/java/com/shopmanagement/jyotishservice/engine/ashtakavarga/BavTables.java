package com.shopmanagement.jyotishservice.engine.ashtakavarga;

import java.util.List;

import com.shopmanagement.jyotishservice.engine.model.Planet;

/**
 * Classical Parashara Bhinnashtakavarga contribution tables: houses (1–12) counted from the
 * contributor that receive a bindu in the subject's BAV. Contributor {@code null} = Lagna.
 *
 * <p>Rahu/Ketu are not contributors in classical BAV.
 */
final class BavTables {

  private BavTables() {}

  static List<Integer> houses(Planet subject, Planet contributor) {
    return switch (subject) {
      case SUN -> sun(contributor);
      case MOON -> moon(contributor);
      case MARS -> mars(contributor);
      case MERCURY -> mercury(contributor);
      case JUPITER -> jupiter(contributor);
      case VENUS -> venus(contributor);
      case SATURN -> saturn(contributor);
      default -> List.of();
    };
  }

  private static List<Integer> sun(Planet c) {
    if (c == null) {
      return List.of(3, 4, 6, 10, 11, 12);
    }
    return switch (c) {
      case SUN, MARS, SATURN -> List.of(1, 2, 4, 7, 8, 9, 10, 11);
      case MOON -> List.of(3, 6, 10, 11);
      case MERCURY -> List.of(3, 5, 6, 9, 10, 11, 12);
      case JUPITER -> List.of(5, 6, 9, 11);
      case VENUS -> List.of(6, 7, 12);
      default -> List.of();
    };
  }

  private static List<Integer> moon(Planet c) {
    if (c == null) {
      return List.of(3, 6, 10, 11);
    }
    return switch (c) {
      case SUN -> List.of(3, 6, 7, 8, 10, 11);
      case MOON -> List.of(1, 3, 6, 7, 10, 11);
      case MARS -> List.of(2, 3, 5, 6, 9, 10, 11);
      case MERCURY -> List.of(1, 3, 4, 5, 7, 8, 10, 11);
      case JUPITER -> List.of(1, 4, 7, 8, 10, 11, 12);
      case VENUS -> List.of(3, 4, 5, 7, 9, 10, 11);
      case SATURN -> List.of(3, 5, 6, 11);
      default -> List.of();
    };
  }

  private static List<Integer> mars(Planet c) {
    if (c == null) {
      return List.of(1, 3, 6, 10, 11);
    }
    return switch (c) {
      case SUN -> List.of(3, 5, 6, 10, 11);
      case MOON -> List.of(3, 6, 10, 11);
      case MARS -> List.of(1, 2, 4, 7, 8, 10, 11);
      case MERCURY -> List.of(3, 5, 6, 9, 11);
      case JUPITER -> List.of(6, 10, 11, 12);
      case VENUS -> List.of(6, 8, 11, 12);
      case SATURN -> List.of(1, 4, 7, 8, 9, 10, 11);
      default -> List.of();
    };
  }

  private static List<Integer> mercury(Planet c) {
    if (c == null) {
      return List.of(1, 2, 4, 6, 8, 10, 11);
    }
    return switch (c) {
      case SUN -> List.of(5, 6, 9, 11, 12);
      case MOON -> List.of(2, 4, 6, 8, 10, 11);
      case MARS -> List.of(1, 2, 4, 7, 8, 9, 10, 11);
      case MERCURY -> List.of(1, 3, 5, 6, 9, 10, 11, 12);
      case JUPITER -> List.of(6, 8, 11, 12);
      case VENUS -> List.of(1, 2, 3, 4, 5, 8, 9, 11);
      case SATURN -> List.of(1, 2, 4, 7, 8, 9, 10, 11);
      default -> List.of();
    };
  }

  private static List<Integer> jupiter(Planet c) {
    if (c == null) {
      return List.of(1, 2, 4, 5, 6, 7, 9, 10, 11);
    }
    return switch (c) {
      case SUN -> List.of(1, 2, 3, 4, 7, 8, 9, 10, 11);
      case MOON -> List.of(2, 5, 7, 9, 11);
      case MARS -> List.of(1, 2, 4, 7, 8, 9, 10, 11);
      case MERCURY -> List.of(1, 2, 4, 5, 6, 9, 10, 11);
      case JUPITER -> List.of(1, 2, 3, 4, 7, 8, 10, 11);
      case VENUS -> List.of(2, 5, 6, 9, 10, 11);
      case SATURN -> List.of(3, 5, 6, 12);
      default -> List.of();
    };
  }

  private static List<Integer> venus(Planet c) {
    if (c == null) {
      return List.of(1, 2, 3, 4, 5, 8, 9, 11);
    }
    return switch (c) {
      case SUN -> List.of(8, 11, 12);
      case MOON -> List.of(1, 2, 3, 4, 5, 8, 9, 11, 12);
      case MARS -> List.of(3, 5, 6, 9, 11, 12);
      case MERCURY -> List.of(3, 5, 6, 9, 11);
      case JUPITER -> List.of(5, 8, 9, 10, 11);
      case VENUS -> List.of(1, 2, 3, 4, 5, 8, 9, 11);
      case SATURN -> List.of(3, 4, 5, 8, 9, 10, 11);
      default -> List.of();
    };
  }

  private static List<Integer> saturn(Planet c) {
    if (c == null) {
      return List.of(1, 3, 4, 6, 10, 11);
    }
    return switch (c) {
      case SUN -> List.of(1, 2, 4, 7, 8, 9, 10, 11);
      case MOON -> List.of(3, 6, 11);
      case MARS -> List.of(3, 5, 6, 10, 11, 12);
      case MERCURY -> List.of(6, 8, 9, 10, 11, 12);
      case JUPITER -> List.of(5, 6, 11, 12);
      case VENUS -> List.of(6, 11, 12);
      case SATURN -> List.of(3, 5, 6, 11);
      default -> List.of();
    };
  }
}
