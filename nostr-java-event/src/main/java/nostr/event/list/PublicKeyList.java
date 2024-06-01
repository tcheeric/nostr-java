
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import nostr.base.FNostrList;
import nostr.base.PublicKey;
import nostr.event.json.deserializer.CustomPublicKeyListDeserializer;

import java.util.List;

@JsonDeserialize(using = CustomPublicKeyListDeserializer.class)
public class PublicKeyList extends FNostrList<PublicKey> {
    public PublicKeyList() {
        super();
    }

    public PublicKeyList(PublicKey publicKey) {
        this(List.of(publicKey));
    }

    public PublicKeyList(@NonNull List<PublicKey> list) {
        super.addAll(list);
    }
}
