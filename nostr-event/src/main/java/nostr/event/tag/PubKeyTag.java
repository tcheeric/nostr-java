/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.Tag;
import nostr.base.PublicKey;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import nostr.event.BaseTag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Log
@Tag(code = "p")
@ToString
public class PubKeyTag extends BaseTag {

    @Key
    private PublicKey publicKey;

    @Key
    private String mainRelayUrl;

    @Key
    @NIPSupport(2)
    private String petName;

    private PubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
        this.publicKey = publicKey;
        this.mainRelayUrl = mainRelayUrl;
        this.petName = petName;
    }
}
