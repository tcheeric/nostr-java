module nostr.api {
    requires nostr.base;
    requires nostr.util;
    requires nostr.event;
    requires nostr.id;
    requires nostr.client;
    requires nostr.encryption;

  requires lombok;
  requires nostr.crypto;
    requires org.apache.commons.lang3;
  requires com.fasterxml.jackson.module.afterburner;
  requires com.fasterxml.jackson.databind;
  requires java.logging;

  exports nostr.api;
}
