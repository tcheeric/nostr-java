package nostr.controller;

import nostr.context.Context;
import nostr.util.thread.Task;

public interface Controller extends Task<Void> {

    void initialize();

    void handleRequest(Context requestContext);
}
