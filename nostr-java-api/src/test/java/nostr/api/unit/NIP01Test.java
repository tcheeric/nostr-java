package nostr.api.unit;

import nostr.api.NIP01;
import nostr.event.BaseTag;
import nostr.event.entities.UserProfile;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.InternetIdentifierMetadataEvent;
import nostr.event.impl.TextNoteEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class NIP01Test {

    @Test
    public void testGenerateSignValidateAndConvertTextNote() {
        // Step 1: Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        // Step 2: Generate a text note as a GenericEvent
        String content = "This is a test text note.";
        GenericEvent genericEvent = nip01.createTextNoteEvent(content).sign().getEvent();

        // Step 3: Convert the GenericEvent to a TextNoteEvent
        TextNoteEvent textNoteEvent = GenericEvent.convert(genericEvent, TextNoteEvent.class);

        // Step 4: Validate the signed event
        assertDoesNotThrow(() -> textNoteEvent.validate(), "The textNoteEvent validation should not throw an AssertionError.");

        // Step 5: Assert the conversion and content
        assertInstanceOf(TextNoteEvent.class, textNoteEvent, "The converted event should be a TextNoteEvent.");
        assertEquals(content, textNoteEvent.getContent(), "The content of the TextNoteEvent should match the original content.");
        assertEquals(sender.getPublicKey(), textNoteEvent.getPubKey(), "The public key of the TextNoteEvent should match the sender's public key.");
    }

    @Test
    public void testGenerateSignValidateAndConvertTextNoteWithRecipient() {
        // Step 1: Prepare
        Identity sender = Identity.generateRandomIdentity();
        Identity recipient = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        // Step 2: Generate a text note with a recipient as a GenericEvent
        String content = "This is a test text note with a recipient.";
        BaseTag recipientTag = NIP01.createPubKeyTag(recipient.getPublicKey());
        GenericEvent genericEvent = nip01.createTextNoteEvent(List.of(recipientTag), content).sign().getEvent();

        // Step 3: Convert the GenericEvent to a TextNoteEvent
        TextNoteEvent textNoteEvent = GenericEvent.convert(genericEvent, TextNoteEvent.class);

        // Step 4: Validate the signed event
        assertDoesNotThrow(() -> textNoteEvent.validate(), "The textNoteEvent validation should not throw an AssertionError.");

        // Step 5: Assert the conversion, content, and recipient
        assertInstanceOf(TextNoteEvent.class, textNoteEvent, "The converted event should be a TextNoteEvent.");
        assertEquals(content, textNoteEvent.getContent(), "The content of the TextNoteEvent should match the original content.");
        assertEquals(sender.getPublicKey(), textNoteEvent.getPubKey(), "The public key of the TextNoteEvent should match the sender's public key.");
        assertEquals(1, textNoteEvent.getRecipients().size(), "The TextNoteEvent should have exactly one recipient.");
        assertEquals(recipient.getPublicKey(), textNoteEvent.getRecipients().get(0), "The recipient's public key should match the one in the GenericEvent.");
    }

    @Test
    public void testCreateTextNoteEventWithRecipientListParameter() {
        Identity sender = Identity.generateRandomIdentity();
        Identity recipient = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        PubKeyTag recipientTag = new PubKeyTag(recipient.getPublicKey());
        GenericEvent genericEvent = nip01.createTextNoteEvent("Generic", List.of(recipientTag)).sign().getEvent();

        TextNoteEvent textNoteEvent = GenericEvent.convert(genericEvent, TextNoteEvent.class);

        assertEquals(1, textNoteEvent.getRecipients().size());
        assertEquals(recipient.getPublicKey(), textNoteEvent.getRecipients().get(0));
    }

    @Test
    public void testGenerateSignValidateAndConvertMetadataEvent() throws MalformedURLException {
        // Step 1: Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        // Step 2: Generate a metadata event
        UserProfile userProfile = UserProfile.builder()
                .nip05("testuser@nos.tr")
                .name("test user")
                .about("This is a test user profile.")
                .picture(URI.create("https://example.com/profile.jpg").toURL())
                .build();
        GenericEvent genericEvent = nip01.createMetadataEvent(userProfile).sign().getEvent();

        InternetIdentifierMetadataEvent metadataEvent = GenericEvent.convert(genericEvent, InternetIdentifierMetadataEvent.class);

        // Step 3: Validate the signed event
        assertDoesNotThrow(() -> metadataEvent.validate(), "The metadata event validation should not throw an AssertionError.");

        // Step 4: Assert the sender
        assertEquals(sender.getPublicKey(), genericEvent.getPubKey(), "The public key of the metadata event should match the sender's public key.");
    }

    @Test
    public void testGenerateSignValidateAndConvertReplaceableEvent() {
        // Step 1: Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        // Step 2: Generate a replaceable event
        int kind = 10001;
        String content = "This is a replaceable event.";
        GenericEvent genericEvent = nip01.createReplaceableEvent(kind, content).sign().getEvent();

        // Step 3: Validate the signed event
        //assertDoesNotThrow(() -> ((ReplaceableEvent) genericEvent).validate(), "The replaceable event validation should not throw an AssertionError.");

        // Step 4: Assert the kind, content, and sender
        assertEquals(kind, genericEvent.getKind(), "The kind of the replaceable event should match the specified kind.");
        assertEquals(content, genericEvent.getContent(), "The content of the replaceable event should match the original content.");
        assertEquals(sender.getPublicKey(), genericEvent.getPubKey(), "The public key of the replaceable event should match the sender's public key.");
    }

    @Test
    public void testGenerateSignValidateAndConvertEphemeralEvent() {
        // Step 1: Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        // Step 2: Generate an ephemeral event
        int kind = 20000;
        String content = "This is an ephemeral event.";
        GenericEvent genericEvent = nip01.createEphemeralEvent(kind, content).sign().getEvent();

        // Step 3: Validate the signed event
        //assertDoesNotThrow(() -> ((EphemeralEvent) genericEvent).validate(), "The ephemeral event validation should not throw an AssertionError.");

        // Step 4: Assert the kind, content, and sender
        assertEquals(kind, genericEvent.getKind(), "The kind of the ephemeral event should match the specified kind.");
        assertEquals(content, genericEvent.getContent(), "The content of the ephemeral event should match the original content.");
        assertEquals(sender.getPublicKey(), genericEvent.getPubKey(), "The public key of the ephemeral event should match the sender's public key.");
    }

    @Test
    public void testGenerateSignValidateAndConvertAddressableEvent() {
        // Step 1: Prepare
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        // Step 2: Generate a parameterized replaceable event
        int kind = 30000;
        String content = "This is a parameterized replaceable event.";
        GenericEvent genericEvent = nip01.createAddressableEvent(kind, content).sign().getEvent();

        // Step 3: Validate the signed event
        //assertDoesNotThrow(() -> ((AddressableEvent) genericEvent).validate(), "The parameterized replaceable event validation should not throw an AssertionError.");

        // Step 4: Assert the kind, content, and sender
        assertEquals(kind, genericEvent.getKind(), "The kind of the parameterized replaceable event should match the specified kind.");
        assertEquals(content, genericEvent.getContent(), "The content of the parameterized replaceable event should match the original content.");
        assertEquals(sender.getPublicKey(), genericEvent.getPubKey(), "The public key of the parameterized replaceable event should match the sender's public key.");
    }

    @Test
    public void testCreateAddressableEventWithTagList() {
        Identity sender = Identity.generateRandomIdentity();
        NIP01 nip01 = new NIP01(sender);

        GenericTag tag = new GenericTag("test");
        GenericEvent event = nip01.createAddressableEvent(List.of(tag), 30001, "addr").sign().getEvent();

        assertEquals(1, event.getTags().size());
        assertEquals("addr", event.getContent());
    }

    @Test
    public void testCreateEventTag() {
        String eventId = "test-event-id";
        BaseTag genericTag = NIP01.createEventTag(eventId);

        assertInstanceOf(EventTag.class, genericTag, "The created tag should be a EventTag.");
        assertEquals("e", genericTag.getCode(), "The tag code should be 'e' for event tags.");
        assertEquals(eventId, ((EventTag) genericTag).getIdEvent(), "The event ID should match the provided value.");
    }

    @Test
    public void testCreatePubKeyTag() {
        Identity identity = Identity.generateRandomIdentity();
        PubKeyTag pubKeyTag = (PubKeyTag) NIP01.createPubKeyTag(identity.getPublicKey());

        assertInstanceOf(PubKeyTag.class, pubKeyTag, "The created tag should be a PubKeyTag.");
        assertEquals("p", pubKeyTag.getCode(), "The tag code should be 'p' for pubkey tags.");
        assertEquals(identity.getPublicKey(), pubKeyTag.getPublicKey(), "The public key should match the provided value.");
    }

    @Test
    public void testCreateIdentifierTag() {
        String identifier = "test-identifier";
        IdentifierTag identifierTag = (IdentifierTag) NIP01.createIdentifierTag(identifier);

        assertInstanceOf(IdentifierTag.class, identifierTag, "The created tag should be an IdentifierTag.");
        assertEquals("d", identifierTag.getCode(), "The tag code should be 'd' for identifier tags.");
        assertEquals(identifier, identifierTag.getUuid(), "The identifier should match the provided value.");
    }

    @Test
    public void testCreateAddressTag() {
        Integer kind = 1;
        Identity identity = Identity.generateRandomIdentity();
        String identifier = "test-identifier";
        AddressTag addressTag = (AddressTag) NIP01.createAddressTag(kind, identity.getPublicKey(), identifier);

        assertInstanceOf(AddressTag.class, addressTag, "The created tag should be an AddressTag.");
        assertEquals("a", addressTag.getCode(), "The tag code should be 'a' for address tags.");
        assertEquals(kind, addressTag.getKind(), "The kind should match the provided value.");
        assertEquals(identity.getPublicKey(), addressTag.getPublicKey(), "The public key should match the provided value.");
        assertEquals(identifier, addressTag.getIdentifierTag().getUuid(), "The identifier should match the provided value.");
    }
}
