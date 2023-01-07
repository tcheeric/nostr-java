/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "p")
public class PubKeyTag extends BaseTag {

    @Key
    private PublicKey publicKey;

    @Key
    private String mainRelayUrl;

    @Key(nip = 2)
    private String petName;

    private PubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
        this.publicKey = publicKey;
        this.mainRelayUrl = mainRelayUrl;
        this.petName = petName;
    }
}
