package nostr.controller;

import nostr.context.Context;

public interface Controller {

    void initialize();

    void handleRequest(Context requestContext);
}
