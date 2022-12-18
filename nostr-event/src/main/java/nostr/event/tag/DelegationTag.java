package nostr.event.tag;

import nostr.base.ISignable;
import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.base.PublicKey;
import nostr.base.Signature;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import java.beans.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;

/**
 *
 * @author squirrel
 */
@Data
@Log
@EqualsAndHashCode(callSuper = false)
@Tag(code = "delegation")
@NIPSupport(26)
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
