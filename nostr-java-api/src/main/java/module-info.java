/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module nostr.api {
    requires nostr.base;
    requires nostr.util;
    requires nostr.crypto;
    requires nostr.event;
    requires nostr.id;
    
    requires lombok;
    requires java.logging;
    /*requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires org.bouncycastle.provider;*/

    exports nostr.api;
}
