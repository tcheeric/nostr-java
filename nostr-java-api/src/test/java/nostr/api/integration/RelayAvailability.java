package nostr.api.integration;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ResourceBundle;

public class RelayAvailability {
    public static boolean areRelaysAvailable() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("relays");
            for (String key : bundle.keySet()) {
                String uri = bundle.getString(key);
                if (!isRelayAvailable(uri)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isRelayAvailable(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                String scheme = uri.getScheme();
                if ("wss".equalsIgnoreCase(scheme)) {
                    port = 443;
                } else if ("ws".equalsIgnoreCase(scheme)) {
                    port = 80;
                } else {
                    throw new IllegalArgumentException("Unsupported URI scheme: " + scheme);
                }
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 1000);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
