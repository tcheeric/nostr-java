package nostr.mcp.argument;

import lombok.NonNull;
import nostr.mcp.tool.ToolFailure;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A point in time, however the caller chose to express it.
 *
 * <p>Relays speak Unix seconds, but a model asked to "catch up on the last day" writes
 * {@code "24h"}, and a user quoting a date writes an ISO-8601 string. Accepting all three and
 * normalising here means the relay boundary sees one representation and the tools never do
 * arithmetic on a string.
 *
 * <p>Relative expressions are resolved against an injected clock, so tests state an exact
 * expectation instead of asserting a range around "now".
 */
public final class TimeArgument {

  private static final Pattern RELATIVE =
      Pattern.compile("^(\\d+)\\s*([smhdw])$", Pattern.CASE_INSENSITIVE);

  private TimeArgument() {}

  /**
   * Normalise a timestamp argument to Unix seconds.
   *
   * @param argumentName the argument being decoded, so an error names what the caller wrote
   * @param value ISO-8601, a bare date, a relative expression such as {@code 24h}, or Unix
   *     seconds
   * @param clock what "now" means, for resolving relative expressions
   * @return the instant in Unix seconds
   * @throws nostr.mcp.tool.ToolException when the value is in none of those forms
   */
  public static long toUnixSeconds(
      @NonNull String argumentName, @NonNull String value, @NonNull Clock clock) {
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(argumentName + " is empty");
    }
    Matcher relative = RELATIVE.matcher(trimmed);
    if (relative.matches()) {
      return relativeToNow(relative, clock);
    }
    return absolute(argumentName, trimmed);
  }

  /**
   * Reads a relative expression as an age, not a future time.
   *
   * <p>"24h" in a query always means the last 24 hours: an agent asking for events cannot mean
   * a day from now, because no relay holds events that have not happened.
   */
  private static long relativeToNow(Matcher relative, Clock clock) {
    long amount = Long.parseLong(relative.group(1));
    Duration unit =
        switch (relative.group(2).toLowerCase(Locale.ROOT)) {
          case "s" -> Duration.ofSeconds(1);
          case "m" -> Duration.ofMinutes(1);
          case "h" -> Duration.ofHours(1);
          case "d" -> Duration.ofDays(1);
          default -> Duration.ofDays(7);
        };
    return clock.instant().minus(unit.multipliedBy(amount)).getEpochSecond();
  }

  private static long absolute(String argumentName, String value) {
    if (value.chars().allMatch(Character::isDigit)) {
      return Long.parseLong(value);
    }
    try {
      return Instant.parse(value).getEpochSecond();
    } catch (DateTimeException notAnInstant) {
      return startOfDay(argumentName, value);
    }
  }

  /**
   * Reads a bare date as the start of that day in UTC.
   *
   * <p>A user writing "2026-01-01" means the whole day, and taking its start makes {@code since}
   * inclusive of it, which is what they meant.
   */
  private static long startOfDay(String argumentName, String value) {
    try {
      return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    } catch (DateTimeException notADate) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          argumentName
              + " should be a relative age such as '24h' or '7d', an ISO-8601 timestamp, a date"
              + " such as '2026-01-01', or Unix seconds, but was '"
              + value
              + "'");
    }
  }
}
