package nostr.client.springwebsocket;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/** Common retry configuration for WebSocket send operations. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Retryable(
    value = IOException.class,
    maxAttempts = NostrRetryable.MAX_ATTEMPTS,
    backoff = @Backoff(delay = NostrRetryable.DELAY, multiplier = NostrRetryable.MULTIPLIER))
public @interface NostrRetryable {
  int MAX_ATTEMPTS = 3;
  long DELAY = 500L;
  double MULTIPLIER = 2.0;
}
