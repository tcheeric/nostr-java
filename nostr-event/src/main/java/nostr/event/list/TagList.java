/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.list;

import nostr.base.ITag;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.JsonList;

/**
 *
 * @author squirrel
 */
@SuppressWarnings("rawtypes")
@Log
@JsonList
public class TagList extends BaseList {

    @SuppressWarnings("unchecked")
    public TagList() {
        this(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    protected TagList(@NonNull List<? extends ITag> list) {
        super(list);
    }

}
