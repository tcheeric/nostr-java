package nostr.ws;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.Relay;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientConnectionFactory;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.JettyUpgradeListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * @author squirrel
 */
@Log
@ToString
public class Connection {

    @ToString.Exclude
    private WebSocketClient webSocketClient;

    @ToString.Exclude
    private Session session;

    @Getter
    @ToString.Include
    private final Relay relay;

    @ToString.Exclude
    private HttpClient httpClient;
    //private List<Response> responses;

    public Connection(@NonNull Relay relay, @NonNull RequestContext context/*, @NonNull List<Response> responses*/) {
        //this.responses = responses;
        this.relay = relay;
        this.connect(context);
    }

    public void stop(@NonNull RequestContext context) {
        log.log(Level.INFO, "Closing the session to {0}", relay.toString());
        RelayClientListenerEndPoint clientEndPoint = RelayClientListenerEndPoint.getInstance(context);
        if (!clientEndPoint.isConnected(relay)) {
            log.log(Level.INFO, "The session is already closed to {0}", relay.toString());
            return;
        }
        new Thread(() -> LifeCycle.stop(webSocketClient)).start();
        clientEndPoint.onClose(StatusCode.NORMAL, "Client closed", session);
    }

    public void connect(@NonNull RequestContext context) {
        RelayClientListenerEndPoint clientEndPoint = RelayClientListenerEndPoint.getInstance(context);

        if (clientEndPoint.isConnected(relay)) {
            log.log(Level.INFO, "The session is already open to {0}. Aborting...", relay.toString());
            this.session = clientEndPoint.getSession(relay);
            return;
        }

        log.log(Level.INFO, "Opening a session to {0}", relay.getHostname());

        if (context instanceof DefaultRequestContext) {
            if (relay.getURI().getScheme().equals("wss")) {
                SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
                sslContextFactory.setIncludeProtocols("TLSv1.3");
                ClientConnector clientConnector = new ClientConnector();
                clientConnector.setSslContextFactory(sslContextFactory);

                ClientConnectionFactory.Info h1 = HttpClientConnectionFactory.HTTP11;
                ClientConnectionFactory.Info h2 = new ClientConnectionFactoryOverHTTP2.HTTP2(new HTTP2Client(clientConnector));

                // Create the HttpClientTransportDynamic, preferring h2 over h1.
                HttpClientTransport transport = new HttpClientTransportDynamic(clientConnector, h1, h2);
                httpClient = new HttpClient(transport);
            } else if (relay.getURI().getScheme().equals("ws")) {
                httpClient = new HttpClient();
            } else {
                throw new RuntimeException();
            }

            var webSocketClient = new WebSocketClient(httpClient);
            try {
                webSocketClient.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Create a custom HTTP request.
            ClientUpgradeRequest customRequest = new ClientUpgradeRequest();
            customRequest.setHeader(HttpHeader.UPGRADE.asString(), "h2c");
            customRequest.setHeader(HttpHeader.CONNECTION.asString(), "Upgrade, HTTP2-Settings");

            // The listener to inspect the HTTP response.
            JettyUpgradeListener listener = new JettyUpgradeListener() {

                @Override
                public void onHandshakeRequest(HttpRequest request) {
                    request.getHeaders().forEach((field)
                            -> {
                        log.log(Level.FINEST, "request header: {0}={1}", new Object[]{field.getName(), field.getValue()});
                    });
                }

                @Override
                public void onHandshakeResponse(HttpRequest request, HttpResponse response) {

                    response.getHeaders().forEach((field)
                            -> {
                        log.log(Level.FINEST, "response header: {0}={1}", new Object[]{field.getName(), field.getValue()});
                    });
                }
            };

            CompletableFuture<Session> clientSessionPromise;
            try {
                clientSessionPromise = webSocketClient.connect(clientEndPoint, relay.getURI(), customRequest, listener);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            try {
                this.session = clientSessionPromise.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            log.log(Level.INFO, "The session {0} -> {1} is now open", new Object[]{relay, session.getRemoteAddress()});
        }
    }
}
