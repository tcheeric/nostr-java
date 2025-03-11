
module nostr.base {
  requires static lombok;
  requires nostr.util;
  requires nostr.crypto;
  requires com.fasterxml.jackson.module.afterburner;
  requires com.fasterxml.jackson.databind;
  requires java.logging;

  exports nostr.base;
  exports nostr.base.annotation;
}
