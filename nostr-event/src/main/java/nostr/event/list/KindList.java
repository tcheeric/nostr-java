/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.list;

import com.tcheeric.nostr.base.annotation.JsonList;
import nostr.event.Kind;
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
public class KindList extends BaseList<Kind> {

    public KindList() {
        this(new ArrayList<>());
    }

    private KindList(@NonNull List<Kind> list) {
        super(list);
    }
}
