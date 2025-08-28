package nostr.util.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import nostr.util.NostrException;
import nostr.util.http.HttpClientProvider;
import org.junit.jupiter.api.Test;

public class Nip05ValidatorTest {

  /* Ensures validation fails for illegal characters in the local-part. */
  @Test
  public void testInvalidLocalPart() {
    Nip05Validator validator =
        Nip05Validator.builder().nip05("bad!part@example.com").publicKey("pub").build();
    assertThrows(NostrException.class, validator::validate);
  }

  /* Ensures domains containing schemes are rejected. */
  @Test
  public void testInvalidDomain() {
    Nip05Validator validator =
        Nip05Validator.builder().nip05("user@http://example.com").publicKey("pub").build();
    assertThrows(NostrException.class, validator::validate);
  }

  /* Validates that a matching public key passes successfully. */
  @Test
  public void testSuccessfulValidation() {
    HttpResponse<String> resp = new MockHttpResponse(200, "{\"names\":{\"alice\":\"pub\"}}");
    HttpClient client = new MockHttpClient(resp);
    Nip05Validator validator =
        Nip05Validator.builder()
            .nip05("alice@example.com")
            .publicKey("pub")
            .httpClientProvider(new FixedHttpClientProvider(client))
            .build();
    assertDoesNotThrow(validator::validate);
  }

  /* Detects when the returned public key does not match the expected one. */
  @Test
  public void testMismatchedPublicKey() {
    HttpResponse<String> resp = new MockHttpResponse(200, "{\"names\":{\"alice\":\"wrong\"}}");
    HttpClient client = new MockHttpClient(resp);
    Nip05Validator validator =
        Nip05Validator.builder()
            .nip05("alice@example.com")
            .publicKey("pub")
            .httpClientProvider(new FixedHttpClientProvider(client))
            .build();
    assertThrows(NostrException.class, validator::validate);
  }

  /* Propagates network failures with descriptive messages. */
  @Test
  public void testNetworkFailure() {
    HttpClient client = new MockHttpClient(new IOException("boom"));
    Nip05Validator validator =
        Nip05Validator.builder()
            .nip05("alice@example.com")
            .publicKey("pub")
            .httpClientProvider(new FixedHttpClientProvider(client))
            .build();
    assertThrows(NostrException.class, validator::validate);
  }

  /* Verifies JSON parsing logic of the getPublicKey helper. */
  @Test
  public void testGetPublicKeyViaReflection() throws Exception {
    Nip05Validator validator =
        Nip05Validator.builder().nip05("user@example.com").publicKey("pub").build();
    Method m = Nip05Validator.class.getDeclaredMethod("getPublicKey", String.class, String.class);
    m.setAccessible(true);
    String json = "{\"names\":{\"alice\":\"abc\"}}";
    String result = (String) m.invoke(validator, json, "alice");
    assertEquals("abc", result);
    String missing = (String) m.invoke(validator, json, "bob");
    assertNull(missing);
  }

  private static class FixedHttpClientProvider implements HttpClientProvider {
    private final HttpClient client;

    FixedHttpClientProvider(HttpClient client) {
      this.client = client;
    }

    @Override
    public HttpClient create(Duration connectTimeout) {
      return client;
    }
  }

  private static class MockHttpClient extends HttpClient {
    private final HttpResponse<String> response;
    private final IOException exception;

    MockHttpClient(HttpResponse<String> response) {
      this.response = response;
      this.exception = null;
    }

    MockHttpClient(IOException exception) {
      this.response = null;
      this.exception = exception;
    }

    @Override
    public Optional<java.net.CookieHandler> cookieHandler() {
      return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
      return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
      return Redirect.NEVER;
    }

    @Override
    public Optional<java.net.ProxySelector> proxy() {
      return Optional.empty();
    }

    @Override
    public javax.net.ssl.SSLContext sslContext() {
      return null;
    }

    @Override
    public javax.net.ssl.SSLParameters sslParameters() {
      return null;
    }

    @Override
    public Optional<java.net.Authenticator> authenticator() {
      return Optional.empty();
    }

    @Override
    public Optional<java.util.concurrent.Executor> executor() {
      return Optional.empty();
    }

    @Override
    public HttpClient.Version version() {
      return HttpClient.Version.HTTP_1_1;
    }

    @Override
    public <T> HttpResponse<T> send(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
      if (exception != null) {
        throw exception;
      }
      return (HttpResponse<T>) response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
      return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
      return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }
  }

  private static class MockHttpResponse implements HttpResponse<String> {
    private final int statusCode;
    private final String body;

    MockHttpResponse(int statusCode, String body) {
      this.statusCode = statusCode;
      this.body = body;
    }

    @Override
    public int statusCode() {
      return statusCode;
    }

    @Override
    public String body() {
      return body;
    }

    @Override
    public HttpRequest request() {
      return HttpRequest.newBuilder().uri(URI.create("https://example.com")).build();
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return HttpHeaders.of(Collections.emptyMap(), (s1, s2) -> true);
    }

    @Override
    public URI uri() {
      return URI.create("https://example.com");
    }

    @Override
    public HttpClient.Version version() {
      return HttpClient.Version.HTTP_1_1;
    }

    @Override
    public Optional<javax.net.ssl.SSLSession> sslSession() {
      return Optional.empty();
    }
  }
}
