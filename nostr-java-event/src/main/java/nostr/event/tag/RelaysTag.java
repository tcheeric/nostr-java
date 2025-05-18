package nostr.event.tag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.Relay;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.json.serializer.RelaysTagSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "relays", nip = 57)
@JsonSerialize(using = RelaysTagSerializer.class)
public class RelaysTag extends BaseTag {
    private List<Relay> relays;

    public RelaysTag() {
        this.relays = new ArrayList<>();
    }

    public RelaysTag(@NonNull List<Relay> relays) {
        this.relays = relays;
    }

    public RelaysTag(@NonNull Relay... relays) {
        this(List.of(relays));
    }

    public static <T extends BaseTag> T deserialize(JsonNode node) {
        return (T) new RelaysTag(Optional.ofNullable(node).map(jsonNode -> new Relay(jsonNode.get(1).asText())).orElseThrow());
    }

    public static RelaysTag updateFields(@NonNull GenericTag genericTag) {
        if (!"relays".equals(genericTag.getCode())) {
            throw new IllegalArgumentException("Invalid tag code for RelaysTag");
        }

        List<Relay> relays = new ArrayList<>();
        for (ElementAttribute attribute : genericTag.getAttributes()) {
            relays.add(new Relay(attribute.getValue().toString()));
        }

        RelaysTag relaysTag = new RelaysTag(relays);
        return relaysTag;
    }

}
