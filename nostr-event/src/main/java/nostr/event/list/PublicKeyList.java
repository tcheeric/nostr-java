
package nostr.event.list;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.base.annotation.JsonList;

/**
 *
 * @author squirrel
 */
@Builder
@Log
@JsonList
public class PublicKeyList extends BaseList<PublicKey> {

    public PublicKeyList() {
        this(new ArrayList<>());
    }

    private PublicKeyList(@NonNull List<PublicKey> list) {
        super(list);
    }
}
