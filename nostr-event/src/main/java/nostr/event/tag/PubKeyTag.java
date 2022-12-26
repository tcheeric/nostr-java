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
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.base.annotation.NIPSupport;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

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
