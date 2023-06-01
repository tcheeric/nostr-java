
module nostr.base {
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires nostr.util;
    requires nostr.crypto;
    requires java.logging;
    
    exports nostr.base;
    exports nostr.base.annotation;    
}
