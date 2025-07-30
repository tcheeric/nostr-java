package nostr.client.springwebsocket;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

public class StandardWebSocketClientTest {

    @Test
    void receiveMessageAsFlux() throws Exception {
        DisposableServer server = HttpServer.create()
                .port(0)
                .route(routes -> routes.ws("/test", (in, out) -> out.sendString(Mono.just("ACK"))))
                .bindNow();

        String uri = "ws://localhost:" + server.port() + "/test";
        StandardWebSocketClient client = new StandardWebSocketClient(uri);

        StepVerifier.create(client.send("hello").take(1))
                .expectNext("ACK")
                .thenCancel()
                .verify();

        server.disposeNow();
    }
}
