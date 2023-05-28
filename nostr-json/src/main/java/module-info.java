
module nostr.json {
    requires static lombok;
    requires nostr.base;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires nostr.util;
    requires nostr.crypto;
    requires nostr.types;
    requires java.logging;
    
    exports nostr.json;
    exports nostr.json.parser.impl;
    exports nostr.json.unmarshaller.impl;
}
