/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module nostr.ws.request.handler.provider {
    requires nostr.ws;
    requires org.eclipse.jetty.websocket.jetty.api;
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.base;
    requires nostr.util;
    requires nostr.ws.handler;
    
    exports nostr.ws.request.handler.provider;
    exports nostr.ws.request;

//    provides nostr.ws.handler.spi.IRequestHandler with nostr.ws.request.handler.provider.DefaultRequestHandler;
}
