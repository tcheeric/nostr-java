package nostr.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import nostr.base.Command;
import nostr.base.IEncoder;
import nostr.base.Relay;
import nostr.event.BaseMessage;
import nostr.event.impl.CanonicalAuthenticationEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseEventEncoder;

import java.util.List;
import java.util.Map;

import static nostr.base.IDecoder.I_DECODER_MAPPER_AFTERBURNER;
import static nostr.base.IEncoder.I_ENCODER_MAPPER_AFTERBURNER;

/**
 * @author eric
 */
@Setter
@Getter
public class CanonicalAuthenticationMessage extends BaseAuthMessage {

  @JsonProperty
  private final CanonicalAuthenticationEvent event;

  public CanonicalAuthenticationMessage(CanonicalAuthenticationEvent event) {
    super(Command.AUTH.name());
    this.event = event;
  }
  @Override
  public String encode() throws JsonProcessingException {
    return I_ENCODER_MAPPER_AFTERBURNER.writeValueAsString(
        getArrayNode()
            .add(getCommand())
            .add(I_ENCODER_MAPPER_AFTERBURNER.readTree(
                new BaseEventEncoder<>(getEvent()).encode())));
  }

  @SneakyThrows
  public static <T extends BaseMessage> T decode(@NonNull Map map) {
    var event = I_DECODER_MAPPER_AFTERBURNER.convertValue(map, new TypeReference<GenericEvent>() {});

    List<GenericTag> genericTags = event.getTags().stream()
        .filter(GenericTag.class::isInstance)
        .map(GenericTag.class::cast).toList();

    CanonicalAuthenticationEvent canonEvent = new CanonicalAuthenticationEvent(
        event.getPubKey(),
        getAttributeValue(genericTags, "challenge"),
        new Relay(
            getAttributeValue(genericTags, "relay")));
    canonEvent.setId(map.get("id").toString());

    return (T) new CanonicalAuthenticationMessage(canonEvent);
  }

  private static String getAttributeValue(List<GenericTag> genericTags, String attributeName) {
//    TODO: stream optional
    return genericTags.stream()
        .filter(tag -> tag.getCode().equalsIgnoreCase(attributeName)).map(GenericTag::getAttributes).toList().get(0).get(0).getValue().toString();
  }
}
