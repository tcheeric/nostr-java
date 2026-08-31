package nostr.mcp.argument;

import nostr.mcp.tool.ToolException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies every timestamp form a caller might use normalises to Unix seconds. */
class TimeArgumentTest {

  private static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
  private static final Clock FIXED = Clock.fixed(NOW, ZoneOffset.UTC);

  // Verifies a relative age is resolved against now, which is how a model expresses "recently".
  @Test
  void aRelativeAgeIsResolvedAgainstNow() {
    assertEquals(NOW.minus(Duration.ofHours(24)).getEpochSecond(), seconds("24h"));
    assertEquals(NOW.minus(Duration.ofDays(7)).getEpochSecond(), seconds("7d"));
    assertEquals(NOW.minus(Duration.ofMinutes(30)).getEpochSecond(), seconds("30m"));
    assertEquals(NOW.minus(Duration.ofSeconds(45)).getEpochSecond(), seconds("45s"));
    assertEquals(NOW.minus(Duration.ofDays(14)).getEpochSecond(), seconds("2w"));
  }

  // Verifies a relative age is read as an age rather than a future time, since no relay holds
  // events that have not happened yet.
  @Test
  void aRelativeAgeIsInThePast() {
    assertTrue(seconds("1h") < NOW.getEpochSecond());
  }

  // Verifies an ISO-8601 instant is accepted, since that is what a precise caller writes.
  @Test
  void anIsoInstantIsAccepted() {
    assertEquals(
        Instant.parse("2026-01-01T00:00:00Z").getEpochSecond(), seconds("2026-01-01T00:00:00Z"));
  }

  // Verifies a bare date means the start of that day, so a "since" includes the whole day the
  // user named.
  @Test
  void aBareDateMeansTheStartOfThatDay() {
    assertEquals(Instant.parse("2026-01-01T00:00:00Z").getEpochSecond(), seconds("2026-01-01"));
  }

  // Verifies Unix seconds pass through, since an agent may echo back a value a tool returned.
  @Test
  void unixSecondsPassThrough() {
    assertEquals(1767225600L, seconds("1767225600"));
  }

  // Verifies whitespace and case in a relative expression are tolerated.
  @Test
  void relativeFormattingIsTolerated() {
    assertEquals(seconds("24h"), seconds(" 24H "));
  }

  // Verifies an unparseable value explains the accepted forms rather than failing mutely.
  @Test
  void anUnparseableValueExplainsTheAcceptedForms() {
    ToolException refused = assertThrows(ToolException.class, () -> seconds("last tuesday"));

    assertTrue(refused.getMessage().contains("24h"), refused.getMessage());
    assertTrue(refused.getMessage().contains("since"), refused.getMessage());
  }

  private long seconds(String value) {
    return TimeArgument.toUnixSeconds("since", value, FIXED);
  }
}
