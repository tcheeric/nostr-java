package nostr.event.unit;

import nostr.base.PublicKey;
import nostr.event.impl.ChannelCreateEvent;
import nostr.event.impl.CreateOrUpdateProductEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonContentValidationTest {

    private static final PublicKey PUBKEY = new PublicKey("56adf01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f");

    private static class TestChannelCreateEvent extends ChannelCreateEvent {
        public TestChannelCreateEvent(PublicKey pk, String content) { super(pk, content); }
        public void callValidateContent() { super.validateContent(); }
    }

    private static class TestProductEvent extends CreateOrUpdateProductEvent {
        public TestProductEvent(PublicKey pk, List<nostr.event.BaseTag> tags, String content) { super(pk, tags, content); }
        public void callValidateContent() { super.validateContent(); }
    }

    @Test
    void channelCreateInvalidJson() {
        TestChannelCreateEvent event = new TestChannelCreateEvent(PUBKEY, "{invalid");
        assertThrows(AssertionError.class, event::callValidateContent);
    }

    @Test
    void channelCreateMissingFields() {
        String json = "{\"name\":\"test\"}"; // missing about and picture
        TestChannelCreateEvent event = new TestChannelCreateEvent(PUBKEY, json);
        assertThrows(AssertionError.class, event::callValidateContent);
    }

    @Test
    void productEventInvalidJson() {
        TestProductEvent event = new TestProductEvent(PUBKEY, List.of(), "{invalid");
        assertThrows(AssertionError.class, event::callValidateContent);
    }

    @Test
    void productEventMissingFields() {
        String json = "{\"id\":\"123\",\"currency\":\"USD\",\"price\":10}"; // missing name
        TestProductEvent event = new TestProductEvent(PUBKEY, List.of(), json);
        assertThrows(AssertionError.class, event::callValidateContent);
    }
}
