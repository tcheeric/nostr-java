module nostr.api {
    requires nostr.base;
    requires nostr.util;
    requires nostr.event;
    requires nostr.id;
    requires nostr.client;
    requires nostr.encryption;
    requires nostr.encryption.nip04dm;
    requires nostr.encryption.nip44dm;

    requires com.fasterxml.jackson.databind;

    requires lombok;
    requires java.logging;
    requires nostr.crypto;
    requires org.apache.commons.lang3;

    exports nostr.api;
}
