package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nostr.base.ISignable;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;

import java.beans.Transient;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Tag(code = "delegation", nip = 26)
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"pubkey", "conditions", "signature"})
public class DelegationTag extends GenericTag implements ISignable {

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
        return "nostr:" + getCode() + ":" + delegator.toString() + ":" + conditions;
    }

    @Override
    public Consumer<Signature> getSignatureConsumer() {
        return this::setSignature;
    }

    @Override
    public Supplier<ByteBuffer> getByeArraySupplier() {
        return () -> ByteBuffer.wrap(this.getToken().getBytes(StandardCharsets.UTF_8));
    }
}
