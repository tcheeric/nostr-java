package nostr.event.list;

import com.tcheeric.nostr.base.annotation.JsonList;
import nostr.event.tag.PubKeyTag;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.java.Log;

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
