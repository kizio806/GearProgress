package io.github.kizio806.gearprogress.core.time;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parser and formatter for relative durations such as "7d", "12h", "30m", or composite "1d12h". */
public final class RelativeDurationParser {

  private static final Pattern COMPONENT_PATTERN =
      Pattern.compile(
          "(\\d+)\\s*([smhdw]|sec|second|seconds|min|minute|minutes|hour|hours|day|days|week|weeks)?",
          Pattern.CASE_INSENSITIVE);

  private RelativeDurationParser() {}

  /**
   * Parses a duration string into a {@link Duration}.
   *
   * @param raw the raw duration text
   * @return the parsed duration
   * @throws IllegalArgumentException if the format is invalid or duration is empty
   */
  public static Duration parse(String raw) {
    Objects.requireNonNull(raw, "raw duration must not be null");
    String text = raw.trim();
    if (text.isEmpty()) {
      throw new IllegalArgumentException("duration text cannot be empty");
    }

    if (text.startsWith("P") || text.startsWith("p")) {
      try {
        return Duration.parse(text);
      } catch (Exception ignored) {
        // Fall back to component pattern parser
      }
    }

    Matcher matcher = COMPONENT_PATTERN.matcher(text);
    long totalSeconds = 0;
    boolean matchedAny = false;
    int lastEnd = 0;

    while (matcher.find()) {
      int start = matcher.start();
      if (start != lastEnd && !text.substring(lastEnd, start).trim().isEmpty()) {
        throw new IllegalArgumentException("Unrecognized token in duration: " + text);
      }
      lastEnd = matcher.end();
      matchedAny = true;

      long value = Long.parseLong(matcher.group(1));
      String unit = matcher.group(2);
      if (unit == null) {
        unit = "s";
      }
      unit = unit.toLowerCase(Locale.ROOT);

      long multiplier =
          switch (unit) {
            case "s", "sec", "second", "seconds" -> 1L;
            case "m", "min", "minute", "minutes" -> 60L;
            case "h", "hour", "hours" -> 3600L;
            case "d", "day", "days" -> 86400L;
            case "w", "week", "weeks" -> 7 * 86400L;
            default -> throw new IllegalArgumentException("Unknown duration unit: " + unit);
          };

      totalSeconds = Math.addExact(totalSeconds, Math.multiplyExact(value, multiplier));
    }

    if (!matchedAny || lastEnd < text.length()) {
      throw new IllegalArgumentException("Invalid duration format: " + text);
    }

    return Duration.ofSeconds(totalSeconds);
  }

  /**
   * Formats a duration into a human-readable string such as "2d 4h 15m 30s".
   *
   * @param duration the duration to format
   * @return formatted duration string
   */
  public static String formatHuman(Duration duration) {
    Objects.requireNonNull(duration, "duration must not be null");
    if (duration.isNegative()) {
      return "0s";
    }
    long totalSeconds = duration.toSeconds();
    if (totalSeconds == 0) {
      return "0s";
    }

    long days = totalSeconds / 86400;
    long hours = (totalSeconds % 86400) / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;

    StringBuilder builder = new StringBuilder();
    if (days > 0) {
      builder.append(days).append("d ");
    }
    if (hours > 0) {
      builder.append(hours).append("h ");
    }
    if (minutes > 0) {
      builder.append(minutes).append("m ");
    }
    if (seconds > 0 || builder.isEmpty()) {
      builder.append(seconds).append("s");
    }

    return builder.toString().trim();
  }
}
