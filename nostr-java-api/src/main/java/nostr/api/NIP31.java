package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

public class NIP31 {

    public static BaseTag createAltTag(@NonNull String alt) {
        return new BaseTagFactory(Constants.Tag.ALT_CODE, alt).create();
    }
}
