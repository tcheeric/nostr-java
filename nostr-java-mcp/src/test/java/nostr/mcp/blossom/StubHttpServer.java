package nostr.mcp.blossom;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A real HTTP server on loopback, for the tests that need one.
 *
 * <p>The JDK ships one, so the alternative is mocking {@code HttpClient}, which would assert
 * that this code calls the methods it calls rather than that it speaks HTTP. A stub server can
 * be asked what it actually received, which is the only way to check the headers being sent.
 */
final class StubHttpServer implements AutoCloseable {

  private final HttpServer server;
  private final List<Request> received = new ArrayList<>();

  private StubHttpServer(HttpServer server) {
    this.server = server;
  }

  static StubHttpServer started(Consumer<HttpExchange> handler) {
    try {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      StubHttpServer stub = new StubHttpServer(server);
      server.createContext(
          "/",
          exchange -> {
            stub.received.add(Request.of(exchange));
            handler.accept(exchange);
          });
      server.start();
      return stub;
    } catch (IOException e) {
      throw new UncheckedIOException("Could not start the stub server", e);
    }
  }

  String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  List<Request> received() {
    return List.copyOf(received);
  }

  Request lastRequest() {
    return received.get(received.size() - 1);
  }

  static void respond(HttpExchange exchange, int status, String body) {
    respond(exchange, status, body, "application/json");
  }

  static void respond(HttpExchange exchange, int status, String body, String contentType) {
    try {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", contentType);
      exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
      if (bytes.length > 0) {
        exchange.getResponseBody().write(bytes);
      }
      exchange.close();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not respond", e);
    }
  }

  @Override
  public void close() {
    server.stop(0);
  }

  /**
   * One request the stub saw.
   *
   * @param method the HTTP method
   * @param path the path requested
   * @param authorization the Authorization header, or empty string when absent
   * @param body the request body
   */
  record Request(String method, String path, String authorization, byte[] body) {

    static Request of(HttpExchange exchange) {
      try {
        return new Request(
            exchange.getRequestMethod(),
            exchange.getRequestURI().getPath(),
            exchange.getRequestHeaders().getFirst("Authorization") == null
                ? ""
                : exchange.getRequestHeaders().getFirst("Authorization"),
            exchange.getRequestBody().readAllBytes());
      } catch (IOException e) {
        throw new UncheckedIOException("Could not read the request", e);
      }
    }
  }
}
