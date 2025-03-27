package nostr.event.unit;

import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import org.apache.commons.lang3.function.FailablePredicate;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressTagTest {

    @Test
    void getSupportedFields() {
        Integer kind = 1;
        String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
        PublicKey publicKey = new PublicKey(author);
        IdentifierTag identifierTag = new IdentifierTag("UUID-1");
        Relay relay = new Relay("ws://localhost:8080");

        AddressTag addressTag = new AddressTag();
        addressTag.setKind(kind);
        addressTag.setPublicKey(publicKey);
        addressTag.setIdentifierTag(identifierTag);
        addressTag.setRelay(relay);

//        TODO: refactor below to use baseTag.getFieldValue() instead (after refactoring ref'd method in BaseTag to Optional<String>
        List<Field> fields = addressTag.getSupportedFields();
        anyFieldNameMatch(fields, field -> field.getName().equals("kind"));
        anyFieldNameMatch(fields, field -> field.getName().equals("publicKey"));
        anyFieldNameMatch(fields, field -> field.getName().equals("identifierTag"));
        anyFieldNameMatch(fields, field -> field.getName().equals("relay"));

        anyFieldValueMatch(fields, addressTag, fieldValue -> fieldValue.equals(kind.toString()));
        anyFieldValueMatch(fields, addressTag, fieldValue -> fieldValue.equals(publicKey.toString()));
        anyFieldValueMatch(fields, addressTag, fieldValue -> fieldValue.equals(identifierTag.toString()));
        anyFieldValueMatch(fields, addressTag, fieldValue -> fieldValue.equals(relay.toString()));

        assertFalse(fields.stream().anyMatch(field -> field.getName().equals("idEventXXX")));
        assertFalse(Streams.failableStream(fields.stream()).map(addressTag::getFieldValue).anyMatch(fieldValue -> fieldValue.equals(identifierTag.toString() + "x")));
    }

    private static void anyFieldNameMatch(List<Field> fields, Predicate<Field> predicate) {
        assertTrue(fields.stream().anyMatch(predicate));
    }

    private static void anyFieldValueMatch(List<Field> fields, AddressTag addressTag, FailablePredicate<String, Throwable> predicate) {
        assertTrue(Streams.failableStream(fields.stream()).map(addressTag::getFieldValue).anyMatch(predicate));
    }
}
