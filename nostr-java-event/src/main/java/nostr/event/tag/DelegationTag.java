package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import nostr.base.ISignable;
import nostr.base.annotation.Key;
import nostr.event.BaseTag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import java.beans.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@Tag(code = "delegation", nip = 26)
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"pubkey", "conditions", "signature"})
public class DelegationTag extends BaseTag implements ISignable {

    @Key
    @JsonProperty("delegator")
    private PublicKey delegator;

    @Key
    @JsonProperty("conditions")
    private String conditions;

    @Key
    @JsonProperty("token")
    private Signature signature;

    public DelegationTag(PublicKey delegator, String conditions) {
        this.delegator = delegator;
        this.conditions = conditions == null ? "" : conditions;
    }

    @Transient
    public String getToken() {
        StringBuilder strToken = new StringBuilder();
        strToken.append("nostr:").append(getCode()).append(":").append(delegator.toString()).append(":").append(conditions);
        return strToken.toString();
    }
}
