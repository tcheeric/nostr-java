package nostr.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.client.http.HttpClientConnectionFactory;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.JettyUpgradeListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Relay;

/**
 *
 * @author squirrel
 */
@Log
@Data
public class Connection {

    private WebSocketClient webSocketClient;
    private Session session;

    private final Relay relay;
    //private final URI uri;
    private HttpClient httpClient;

    public Connection(@NonNull Relay relay) throws Exception {
        this.relay = relay;
        log.log(Level.INFO, ">>>>>>>>> Relay: {0}", relay);
        //this.uri = new URI(relay.toString());
        this.connect();
    }

    public void stop() {
        new Thread(() -> LifeCycle.stop(webSocketClient)).start();
    }

    private void connect() throws Exception {
        ClientListenerEndPoint clientEndPoint = new ClientListenerEndPoint();

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
//        	TODO
            throw new RuntimeException();
        }

        webSocketClient = new WebSocketClient(httpClient);
        webSocketClient.start();

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
                    log.log(Level.FINER, "request header: {0}={1}", new Object[]{field.getName(), field.getValue()});
                });
            }

            @Override
            public void onHandshakeResponse(HttpRequest request, HttpResponse response) {

                response.getHeaders().forEach((field)
                        -> {
                    log.log(Level.FINER, "response header: {0}={1}", new Object[]{field.getName(), field.getValue()});
                });
            }
        };

        CompletableFuture<Session> clientSessionPromise = webSocketClient.connect(clientEndPoint, relay.getURI(), customRequest, listener);

        this.session = clientSessionPromise.get();

        log.log(Level.INFO, "The session is now open to {0}", relay.getHostname());
    }

    public String getRelayInformation() throws Exception {
        httpClient.start();

        InputStreamResponseListener listener = new InputStreamResponseListener(); //Required for large responses only
        httpClient.newRequest(relay.getURI()).method(HttpMethod.GET).headers(httpFields -> httpFields.add("Accept", "application/nostr+json")).send(listener);

        Response response = listener.get(5, TimeUnit.SECONDS);

        if (response.getStatus() == 200) {
            final String relayInfo = new String(listener.getInputStream().readAllBytes());
            log.log(Level.FINE, "=====> Response: {0}", relayInfo);
            return relayInfo;
        }

        throw new IOException("The request has failed with the response code: " + response.getStatus());
    }

    public void updateRelayMetadata() throws Exception {
        String strInfo = getRelayInformation();
        log.log(Level.INFO, "Relay information: {0}", strInfo);

        ObjectMapper objectMapper = new ObjectMapper();
        var relayInfoDoc = objectMapper.readValue(strInfo, Relay.RelayInformationDocument.class);

        this.relay.setInformationDocument(relayInfoDoc);        
    }
}
