/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event.list;

import com.tcheeric.nostr.base.annotation.JsonList;
import com.tcheeric.nostr.event.impl.Filters;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Builder
@Log
@JsonList
public class FilterList extends BaseList<Filters> {

    public FilterList() {
        this(new ArrayList<>());
    }

    private FilterList(@NonNull List<Filters> list) {
        super(list);
    }

}
