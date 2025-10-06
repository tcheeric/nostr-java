package nostr.event.message;

import static nostr.base.Encoder.ENCODER_MAPPER_BLACKBIRD;
import static nostr.base.IDecoder.I_DECODER_MAPPER_BLACKBIRD;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.EventEncodingException;
import nostr.event.tag.GenericTag;

/**
 * @author eric
 */
@Setter
@Getter
public class CanonicalAuthenticationMessage extends BaseAuthMessage {

  @JsonProperty private final CanonicalAuthenticationEvent event;

  public CanonicalAuthenticationMessage(CanonicalAuthenticationEvent event) {
    super(Command.AUTH.name());
    this.event = event;
  }

  @Override
  public String encode() throws EventEncodingException {
    try {
      return ENCODER_MAPPER_BLACKBIRD.writeValueAsString(
          JsonNodeFactory.instance
              .arrayNode()
              .add(getCommand())
              .add(ENCODER_MAPPER_BLACKBIRD.readTree(new BaseEventEncoder<>(getEvent()).encode())));
    } catch (JsonProcessingException e) {
      throw new EventEncodingException("Failed to encode canonical authentication message", e);
    }
  }

  // TODO - This needs to be reviewed
  @SuppressWarnings("unchecked")
  public static <T extends BaseMessage> T decode(@NonNull Map map) {
    try {
      var event =
          I_DECODER_MAPPER_BLACKBIRD.convertValue(map, new TypeReference<GenericEvent>() {});

      List<BaseTag> baseTags = event.getTags().stream().filter(GenericTag.class::isInstance).toList();

      CanonicalAuthenticationEvent canonEvent =
          new CanonicalAuthenticationEvent(event.getPubKey(), baseTags, "");

      canonEvent.setId(map.get("id").toString());

      return (T) new CanonicalAuthenticationMessage(canonEvent);
    } catch (IllegalArgumentException ex) {
      throw new EventEncodingException("Failed to decode canonical authentication message", ex);
    }
  }

  private static String getAttributeValue(List<GenericTag> genericTags, String attributeName) {
    //    TODO: stream optional
    return genericTags.stream()
        .filter(tag -> tag.getCode().equalsIgnoreCase(attributeName))
        .map(GenericTag::getAttributes)
        .toList()
        .get(0)
        .get(0)
        .value()
        .toString();
  }
}
