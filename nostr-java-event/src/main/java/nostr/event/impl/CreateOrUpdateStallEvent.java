package nostr.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.entities.Stall;

import java.util.List;

/**
 *
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Create or update a stall", nip = 15)
@NoArgsConstructor
public class CreateOrUpdateStallEvent extends MerchantEvent<Stall> {

    public CreateOrUpdateStallEvent(@NonNull PublicKey sender, @NonNull List<BaseTag> tags, @NonNull String content) {
        super(sender, Kind.STALL_CREATE_OR_UPDATE.getValue(), tags, content);
    }

    @SneakyThrows
    public Stall getStall() {
        return IEvent.MAPPER_AFTERBURNER.readValue(getContent(), Stall.class);
    }

    @Override
    protected Stall getEntity() {
        return getStall();
    }
}
