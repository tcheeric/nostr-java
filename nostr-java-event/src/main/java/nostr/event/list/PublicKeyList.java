
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.json.deserializer.CustomPublicKeyListDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author squirrel
 */
@Builder
@JsonDeserialize(using = CustomPublicKeyListDeserializer.class)
public class PublicKeyList extends BaseList<PublicKey> {

    public PublicKeyList() {
        this(new ArrayList<>());
    }

    public PublicKeyList(PublicKey... publicKeys) {
        super(publicKeys);
    }

    public PublicKeyList(@NonNull List<PublicKey> list) {
        super(new ArrayList<>(list));
    }
}
