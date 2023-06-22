package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.Command;
import nostr.base.IDecoder;
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
import nostr.event.message.RelayAuthMessage;
import nostr.event.message.ReqMessage;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
@Data
@AllArgsConstructor
public class BaseMessageDecoder implements IDecoder<GenericMessage> {

    private final String jsonString;

    @Override
    public GenericMessage decode() throws NostrException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            var msgArr = mapper.readValue(jsonString, Object[].class);
            final String strCmd = msgArr[0].toString();
            final Command command = Command.valueOf(strCmd);
            final Object arg = msgArr[1];
            GenericMessage message = null;

            if (arg == null) {
                throw new AssertionError("arg == null");
            }

            switch (command) {
                case AUTH -> {
                    final BaseAuthMessage authMsg;
                    // Client Auth
                    if (arg instanceof Map map) {
                        var event = mapper.convertValue(map, new TypeReference<ClientAuthenticationEvent>() {
                        });
                        authMsg = new ClientAuthenticationMessage(event);
                    } else {
                        // Relay Auth                        
                        final var challenge = arg.toString();
                        authMsg = new RelayAuthMessage(challenge);
                    }
                    message = authMsg;
                }
                case CLOSE ->
                    message = new CloseMessage(arg.toString());
                case EOSE ->
                    message = new EoseMessage(arg.toString());
                case EVENT -> {
                    if (arg instanceof Map map) {
                        var event = mapper.convertValue(map, new TypeReference<GenericEvent>() {
                        });
                        message = new EventMessage(event);
                    } else {
                        throw new AssertionError("Invalid argument: " + arg);
                    }
                }
                case NOTICE ->
                    message = new NoticeMessage(arg.toString());
                case OK -> {
                    //Boolean duplicate = (Boolean) msgArr[2];
                    if (msgArr[2] instanceof Boolean duplicate) {
                        String msgArg = msgArr[3].toString();
                        message = new OkMessage(arg.toString(), duplicate, msgArg);
                    } else {
                        throw new AssertionError("Invalid argument: " + msgArr[2]);
                    }
                }
                case REQ -> {
                    if (arg instanceof Map map) {
                        var filters = mapper.convertValue(map, new TypeReference<Filters>() {
                        });
                        message = new ReqMessage(arg.toString(), filters);
                    } else {
                        throw new AssertionError("Invalid argument: " + msgArr[2]);
                    }
                }
                default ->
                    throw new AssertionError("Invalid command " + strCmd);
            }
            return message;
        } catch (JsonProcessingException ex) {
            throw new NostrException(ex);
        }
    }

}
