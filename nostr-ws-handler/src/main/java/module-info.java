/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module nostr.ws.handler {
    requires nostr.util;
    requires static lombok;
    requires nostr.base;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires nostr.crypto;
    requires nostr.types;
    requires java.logging;
    
    exports nostr.ws.base.handler;
    exports nostr.ws.base.handler.request;
    exports nostr.ws.base.handler.response;
}
