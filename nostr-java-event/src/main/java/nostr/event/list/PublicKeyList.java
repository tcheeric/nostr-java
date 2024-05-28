
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.INostrList;
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
public class PublicKeyList<T extends PublicKey> extends INostrList<T> {
    private final Class<T> clazz;

    public PublicKeyList() {
        this(new ArrayList<>());
    }

    public PublicKeyList(Class<T> clazz) {
        this(new ArrayList<>(), clazz);
    }

    public PublicKeyList(T... publicKeys) {
        this(List.of(publicKeys));
    }

    public PublicKeyList(@NonNull List<T> list) {
        this(list, (Class<T>) PublicKey.class);
    }

    public PublicKeyList(@NonNull List<T> list, Class<T> clazz) {
        super.addAll(list);
        this.clazz = clazz;
    }
}
