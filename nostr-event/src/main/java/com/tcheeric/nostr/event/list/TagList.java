/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event.list;

import com.tcheeric.nostr.base.ITag;
import com.tcheeric.nostr.base.annotation.JsonList;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.java.Log;

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
