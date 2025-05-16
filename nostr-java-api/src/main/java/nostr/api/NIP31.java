package nostr.api;

import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.event.tag.GenericTag;

public class NIP31 {

    public static GenericTag createAltTag(@NonNull String alt) {
        return new TagFactory("alt", 31, alt).create();
    }
}
