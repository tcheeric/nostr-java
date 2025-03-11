
module nostr.event {
  requires static lombok;
  requires nostr.base;
  requires nostr.crypto;
  requires nostr.util;
  requires com.fasterxml.jackson.module.afterburner;
  requires com.fasterxml.jackson.databind;
  requires java.logging;
  requires java.desktop;

  exports nostr.event;
  exports nostr.event.impl;
  exports nostr.event.message;
  exports nostr.event.json.codec;
  exports nostr.event.json.deserializer;
  exports nostr.event.json.serializer;
  exports nostr.event.tag;
  exports nostr.event.util;
  exports nostr.event.filter;
}
