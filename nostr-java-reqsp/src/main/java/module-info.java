module nostr.reqrsp.impl {

    requires nostr.event;
    requires nostr.ws;
    requires nostr.ws.handler;
    requires nostr.reqsp;

    requires lombok;

    exports nostr.reqsp.impl;
}