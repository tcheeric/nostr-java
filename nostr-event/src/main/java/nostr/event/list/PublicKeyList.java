/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.list;

import com.tcheeric.nostr.base.annotation.JsonList;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.PublicKey;

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
