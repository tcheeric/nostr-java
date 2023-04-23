
module nostr.event {
    requires static lombok;
    requires nostr.json;
    requires nostr.base;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires nostr.crypto;
    requires nostr.util;
    requires nostr.types;
    requires java.logging;
    requires java.desktop;
    
    exports nostr.event;
    exports nostr.event.impl;
    exports nostr.event.list;
    exports nostr.event.marshaller.impl;
    exports nostr.event.message;
    exports nostr.event.serializer;
    exports nostr.event.tag;
    exports nostr.event.unmarshaller.impl;
    exports nostr.event.util;
}
