package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.ElementAttribute;
import nostr.base.IDecoder;
import nostr.event.BaseMessage;
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.message.BaseAuthMessage;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.RelayAuthenticationMessage;
import nostr.event.message.ReqMessage;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class BaseMessageDecoder implements IDecoder<BaseMessage> {

    private final String jsonString;

    @Override
    public BaseMessage decode() throws NostrException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            var msgArr = mapper.readValue(jsonString, Object[].class);
            final String strCmd = msgArr[0].toString();
            final Object arg = msgArr[1];
            BaseMessage message = null;

            if (arg == null) {
                throw new AssertionError("arg == null");
            }

            switch (strCmd) {
                case "AUTH" -> {
                    final BaseAuthMessage authMsg;
                    // Client Auth
                    if (arg instanceof Map map) {
                        var event = mapper.convertValue(map, new TypeReference<ClientAuthenticationEvent>() {
                        });
                        authMsg = new ClientAuthenticationMessage(event);
                    } else {
                        // Relay Auth                        
                        final var challenge = arg.toString();
                        authMsg = new RelayAuthenticationMessage(challenge);
                    }
                    message = authMsg;
                }
                case "CLOSE" ->
                    message = new CloseMessage(arg.toString());
                case "EOSE" ->
                    message = new EoseMessage(arg.toString());
                case "EVENT" -> {
                    if (msgArr.length == 2 && arg instanceof Map map) {
                        var event = mapper.convertValue(map, new TypeReference<GenericEvent>() {
                        });
                        message = new EventMessage(event);
                    } else if (msgArr.length == 3 && arg instanceof String) {
                        var subId = arg.toString();
                        if (msgArr[2] instanceof Map map) {
                            var event = mapper.convertValue(map, new TypeReference<GenericEvent>() {
                            });
                            message = new EventMessage(event, subId);
                        }
                    } else {
                        throw new AssertionError("Invalid argument: " + arg);
                    }
                }
                case "NOTICE" ->
                    message = new NoticeMessage(arg.toString());
                case "OK" -> {
                    if (msgArr.length == 4 && msgArr[2] instanceof Boolean duplicate) {
                        String msgArg = msgArr[3].toString();
                        message = new OkMessage(arg.toString(), duplicate, msgArg);
                    } else {
                        throw new AssertionError("Invalid argument: " + msgArr[2]);
                    }
                }
                // TODO - Cater for more than one filters. Create issue in Github
                case "REQ" -> {
                    if (arg instanceof Map map) {
                        var filters = mapper.convertValue(map, new TypeReference<Filters>() {
                        });
                        message = new ReqMessage(arg.toString(), filters);
                    } else {
                        throw new AssertionError("Invalid argument: " + msgArr[2]);
                    }
                }
                default -> {
                    //throw new AssertionError("Invalid command " + strCmd);
                    // NOTE: Only String attribute suppoeted. It would be impossible to guess the object's type
                    GenericMessage gm = new GenericMessage(strCmd);
                    for (int i = 1; i < msgArr.length; i++) {
                        if (msgArr[i] instanceof String) {
                            gm.addAttribute(ElementAttribute.builder().value(msgArr[i]).build());
                        }
                    }
                }
            }
            return message;
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }

}
