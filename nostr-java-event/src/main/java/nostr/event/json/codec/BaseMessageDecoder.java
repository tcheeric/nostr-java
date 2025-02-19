package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import nostr.base.IDecoder;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericMessage;
import nostr.event.message.CanonicalAuthenticationMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.event.message.ReqMessage;

import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
public class BaseMessageDecoder<T extends BaseMessage> implements IDecoder<T> {
    public static final int MAX_JSON_NODE_THRESHOLD = 99;
    private final ObjectMapper mapper = new ObjectMapper();

    public BaseMessageDecoder() {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @Override
    public T decode(@NonNull String jsonString) throws JsonProcessingException {
        ValidJsonNodeFirstPair validJsonNodeFirstPair = jsonFirstPair(jsonString);
        String command = validJsonNodeFirstPair.formerly_strCmd();
        Object subscriptionId = validJsonNodeFirstPair.formerly_arg(); // subscriptionId

        Object[] msgArr = mapper.readValue(jsonString, Object[].class); // TODO: replace with jsonNode after ReqMessage.decode() is finished

        return switch (command) {
            case "AUTH" -> subscriptionId instanceof Map map ?
                CanonicalAuthenticationMessage.decode(map, mapper) :
                RelayAuthenticationMessage.decode(subscriptionId);
            case "CLOSE" -> CloseMessage.decode(subscriptionId);
            case "EOSE" -> EoseMessage.decode(subscriptionId);
            case "EVENT" -> EventMessage.decode(msgArr, mapper);
            case "NOTICE" -> NoticeMessage.decode(subscriptionId);
            case "OK" -> OkMessage.decode(msgArr);
            case "REQ" -> {
                String filtersJson = jsonSecondPair(jsonString).formerly_msgArr(); // filters
                yield ReqMessage.decode(subscriptionId, List.of(filtersJson));
            }
            default -> GenericMessage.decode(msgArr);
        };
    }

    private ValidJsonNodeFirstPair jsonFirstPair(@NonNull String jsonString) throws JsonProcessingException {
        final JsonNode jsonNode = mapper.readTree(jsonString);

        return new ValidJsonNodeFirstPair(
            jsonNode.get(0).asText(),
            jsonNode.get(1).asText());
    }

    private ValidJsonNodeSecondPair jsonSecondPair(@NonNull String jsonString) throws JsonProcessingException {
        final JsonNode jsonNode = mapper.readTree(jsonString);

        return new ValidJsonNodeSecondPair(
            jsonNode.get(2).toString());
    }

    private record ValidJsonNodeFirstPair(
        @NonNull String formerly_strCmd,
        @NonNull Object formerly_arg) {
    }

    private record ValidJsonNodeSecondPair(
        @NonNull String formerly_msgArr) {
    }
}
