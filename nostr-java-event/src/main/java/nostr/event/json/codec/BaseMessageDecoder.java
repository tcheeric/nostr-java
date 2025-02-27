package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.IntStream;

/**
 * @author eric
 */
public class BaseMessageDecoder<T extends BaseMessage> implements IDecoder<T> {
    public static final int COMMAND_INDEX = 0;
    public static final int ARG_INDEX = 1;
    public static final int FILTERS_START_INDEX = 2;

    private final ObjectMapper mapper = new ObjectMapper();

    public BaseMessageDecoder() {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @Override
    public T decode(@NonNull String jsonString) throws JsonProcessingException {
        ValidJsonNodeFirstPair validJsonNodeFirstPair = json_strCmd_arg(jsonString);
        String command = validJsonNodeFirstPair.formerly_strCmd();
        Object subscriptionId = validJsonNodeFirstPair.formerly_arg();

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
            case "REQ" -> ReqMessage.decode(subscriptionId, json_msgArr(jsonString));
            default -> GenericMessage.decode(msgArr);
        };
    }

    private ValidJsonNodeFirstPair json_strCmd_arg(@NonNull String jsonString) throws JsonProcessingException {
        return new ValidJsonNodeFirstPair(
            mapper.readTree(jsonString).get(COMMAND_INDEX).asText(),
            mapper.readTree(jsonString).get(ARG_INDEX).asText());
    }

    private List<String> json_msgArr(@NonNull String jsonString) throws JsonProcessingException {
        return IntStream.range(FILTERS_START_INDEX, mapper.readTree(jsonString).size())
            .mapToObj(idx -> readTree(jsonString, idx)).toList();
    }

    @SneakyThrows
    private String readTree(String jsonString, int idx) {
        return mapper.readTree(jsonString).get(idx).toString();
    }

    private record ValidJsonNodeFirstPair(
        @NonNull String formerly_strCmd,
        @NonNull Object formerly_arg) {}
}
