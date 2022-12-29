package nostr.event.tag;

import nostr.base.ISignable;
import nostr.base.annotation.Key;
import nostr.event.BaseTag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import java.beans.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
@ToString
public class DelegationTag extends BaseTag implements ISignable {

    @Key
    private PublicKey delegatee;

    @Key
    private String conditions;

    @Key
    private Signature signature;

    public DelegationTag(PublicKey delegatee, String conditions) {
        this.delegatee = delegatee;
        this.conditions = conditions == null ? "" : conditions;
    }

    @Transient
    public String getToken() {
        StringBuilder strToken = new StringBuilder();
        strToken.append("nostr:").append(getCode()).append(":").append(delegatee.toString()).append(":").append(conditions);
        return strToken.toString();
    }
}
