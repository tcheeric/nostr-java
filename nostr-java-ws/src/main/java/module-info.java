
module nostr.ws {
    requires org.eclipse.jetty.websocket.jetty.client;
    requires org.eclipse.jetty.websocket.jetty.api;
    requires org.eclipse.jetty.client;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.util;
    requires org.eclipse.jetty.io;
    requires org.eclipse.jetty.http2.client;
    requires org.eclipse.jetty.http2.http.client.transport;

    requires static lombok;

    requires java.logging;

    requires nostr.base;
    requires nostr.ws.handler;
    requires nostr.util;
    requires nostr.event;
    requires nostr.controller;
    requires nostr.ws.response.handler.provider;
    requires nostr.controller.command;
    requires nostr.context;

    requires com.fasterxml.jackson.databind;
    requires nostr.context.impl;

    exports nostr.ws;
}
