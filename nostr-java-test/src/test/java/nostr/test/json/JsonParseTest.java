package nostr.test.json;

import nostr.base.Command;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.util.NostrException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author eric
 */
public class JsonParseTest {

    @Test
    public void issue39() throws NostrException {
        System.out.println("issue39");

        final String parseTarget
                = "[\"EVENT\","
                + "{"
                + "\"content\":\"直んないわ。まあええか\","
                + "\"created_at\":1686199583,"
                + "\"id\":\"fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712\","
                + "\"kind\":1,"
                + "\"pubkey\":\"8c59239319637f97e007dad0d681e65ce35b1ace333b629e2d33f9465c132608\","
                + "\"sig\":\"9584afd231c52fcbcec6ce668a2cc4b6dc9b4d9da20510dcb9005c6844679b4844edb7a2e1e0591958b0295241567c774dbf7d39a73932877542de1a5f963f4b\","
                + "\"tags\":[]"
                + "}]";
        
        final var message = new BaseMessageDecoder(parseTarget).decode();

        Assertions.assertEquals(Command.EVENT.toString(), message.getCommand());

        final var event = (GenericEvent) (((EventMessage) message).getEvent());
        Assertions.assertEquals(1, event.getKind().intValue());
        Assertions.assertEquals(1686199583, event.getCreatedAt().longValue());
        Assertions.assertEquals("fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712", event.getId());
    }
}
