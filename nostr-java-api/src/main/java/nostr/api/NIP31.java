package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.config.Constants;
import nostr.event.tag.GenericTag;

public class NIP31 {

    public static GenericTag createAltTag(@NonNull String alt) {
        return new GenericTagFactory(Constants.Tag.ALT_CODE, alt).create();
    }
}
