package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
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

import java.util.Map;

/**
 * @author eric
 */
public class BaseMessageDecoder<T extends BaseMessage> implements IDecoder<T> {
    private final ObjectMapper mapper;

    public BaseMessageDecoder() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    @SneakyThrows
    @Override
    public T decode(@NonNull String jsonString) {
        Object[] msgArr = mapper.readValue(jsonString, Object[].class);
        final String strCmd = msgArr[0].toString();
        final Object arg = msgArr[1];

        return switch (strCmd) {
            case "AUTH" -> arg instanceof Map map ?
                CanonicalAuthenticationMessage.decode(map, mapper) :
                RelayAuthenticationMessage.decode(arg);
            case "CLOSE" -> CloseMessage.decode(arg);
            case "EOSE" -> EoseMessage.decode(arg);
            case "EVENT" -> EventMessage.decode(msgArr, mapper);
            case "NOTICE" -> NoticeMessage.decode(arg);
            case "OK" -> OkMessage.decode(msgArr);
            case "REQ" -> ReqMessage.decode(msgArr, mapper);
            default -> GenericMessage.decode(msgArr);
        };
    }
}
