/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
 *
 * @author squirrel
 */
@JsonPropertyOrder({"pubKey", "mainRelayUrl", "petName"})
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "p")
@NoArgsConstructor
public class PubKeyTag extends BaseTag {

    @Key
    @JsonProperty("publicKey")
    private PublicKey publicKey;

    @Key
    @JsonProperty("mainRelayUrl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mainRelayUrl;

    @Key(nip = 2)
    @JsonProperty("petName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String petName;

    public PubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
        this.publicKey = publicKey;
        this.mainRelayUrl = mainRelayUrl;
        this.petName = petName;
    }
}
