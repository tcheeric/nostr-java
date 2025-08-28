package nostr.util.http;

import java.net.http.HttpClient;
import java.time.Duration;

/** Provides {@link HttpClient} instances with configurable timeouts. */
public interface HttpClientProvider {

  /**
   * Create a new {@link HttpClient} with the given connect timeout.
   *
   * @param connectTimeout the connection timeout
   * @return configured HttpClient instance
   */
  HttpClient create(Duration connectTimeout);
}
