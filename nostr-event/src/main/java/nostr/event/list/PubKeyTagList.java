package nostr.event.list;

import nostr.event.tag.PubKeyTag;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.java.Log;
import nostr.base.annotation.JsonList;

/**
 *
 * @author squirrel
 */
@Log
@JsonList
public class PubKeyTagList extends TagList {

    public PubKeyTagList() {
        this(new ArrayList<>());
    }

    private PubKeyTagList(List<PubKeyTag> list) {
        super(list);
    }
}
