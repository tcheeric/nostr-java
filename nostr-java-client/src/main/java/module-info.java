/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module nostr.client {
    requires nostr.event;
    requires static lombok;
    requires java.logging;
    requires nostr.util;
    requires nostr.base;
    requires nostr.id;
    requires nostr.ws;
    requires org.bouncycastle.provider;
    requires nostr.ws.handler;
    requires nostr.ws.request.handler.provider;
    
    exports nostr.client;
}
