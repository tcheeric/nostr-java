module nostr.util {
    requires static lombok;
    requires java.logging;

    requires nostr.context;

    exports nostr.util;
    exports nostr.util.thread;
}
