
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import nostr.base.PublicKey;

/**
 *
 * @author squirrel
 */
@Builder
public class PublicKeyList extends BaseList<PublicKey> {

    public PublicKeyList() {
        this(new ArrayList<>());
    }

    public PublicKeyList(@NonNull List<PublicKey> list) {
        super(new ArrayList<>(list));
    }
}
