/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.list;

import nostr.base.INostrList;
import com.tcheeric.nostr.base.annotation.JsonList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 * @param <T>
 */
@AllArgsConstructor
@Data
@Log
@JsonList
public abstract class BaseList<T> implements INostrList<T> {

    @NonNull
    private final List<T> list;

    @Override
    public void add(@NonNull T elt) {
        if (!list.contains(elt)) {
            this.list.add(elt);
        }
    }

    @Override
    public void addAll(@NonNull INostrList<T> aList) {
        this.list.addAll(aList.getList());
    }

    @Override
    public int size() {
        return this.list.size();
    }
}
