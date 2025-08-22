package nostr.util.http;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Default implementation of {@link HttpClientProvider} using Java's HTTP client.
 */
public class DefaultHttpClientProvider implements HttpClientProvider {

    @Override
    public HttpClient create(Duration connectTimeout) {
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
    }
}

