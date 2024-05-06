
module nostr.id {
    requires static lombok;
    requires nostr.base;
    requires nostr.crypto;
    requires nostr.event;
    requires nostr.util;
    requires java.logging;

    exports nostr.id;
}
