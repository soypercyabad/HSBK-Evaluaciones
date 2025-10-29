package pe.iep.hsbk.evaluaciones.util;

import java.util.Collection;
import java.util.stream.Collectors;

public class Format {

  public static String formatRoles(Collection<?> roles) {
    if (roles == null || roles.isEmpty()) return "";
    return roles.stream()
        .map(r -> (r instanceof Enum) ? ((Enum<?>) r).name() : String.valueOf(r))
        .collect(Collectors.joining(" - "));
  }
}
