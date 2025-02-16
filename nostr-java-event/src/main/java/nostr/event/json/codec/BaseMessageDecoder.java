package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
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
        ValidJsonNode validJsonNode = validateJson(jsonString);
        String command = validJsonNode.formerly_strCmd();
        Object subscriptionId = validJsonNode.formerly_arg(); // subscriptionId
        String filtersJson = validJsonNode.formerly_msgArr(); // filters

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
            case "REQ" -> ReqMessage.decode(subscriptionId, List.of(filtersJson));
            default -> GenericMessage.decode(msgArr);
        };
    }

    private ValidJsonNode validateJson(@NonNull String jsonString) throws JsonProcessingException {
        final JsonNode jsonNode = mapper.readTree(jsonString);

        if (jsonNode.size() > MAX_JSON_NODE_THRESHOLD)
            throw new IllegalArgumentException(
                String.format("BaseMessageDecoder expected max [%d] JSON nodes but received [%s] instead with contents:\n\n[%s]\n",
                    MAX_JSON_NODE_THRESHOLD,
                    jsonNode.size(),
                    jsonNode.toPrettyString()
                ));

        return new ValidJsonNode(
            jsonNode.get(0).asText(),
            jsonNode.get(1).asText(),
            jsonNode.get(2).toString());
    }

    private record ValidJsonNode(@NonNull String formerly_strCmd, @NonNull Object formerly_arg,
                                 @NonNull String formerly_msgArr) {
    }
}
