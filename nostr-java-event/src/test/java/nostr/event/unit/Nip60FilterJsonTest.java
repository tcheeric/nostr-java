package nostr.event.unit;

import nostr.base.GenericTagQuery;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.Filters;
import nostr.event.filter.GenericTagQueryFilter;
import nostr.event.filter.KindFilter;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify NIP-60 filter JSON serialization format.
 */
public class Nip60FilterJsonTest {

    @Test
    public void testNip60FilterJsonFormat() {
        String pubkey = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

        // First filter: kinds + authors (like Nip60SyncService first group)
        Filters filter1 = new Filters(List.of(
            new KindFilter<>(Kind.valueOf(17375)),
            new KindFilter<>(Kind.valueOf(7375)),
            new KindFilter<>(Kind.valueOf(7376)),
            new AuthorFilter<>(new PublicKey(pubkey))
        ));

        // Second filter: kind + #a tag (like Nip60SyncService second group)
        Filters filter2 = new Filters(List.of(
            new KindFilter<>(Kind.valueOf(7375)),
            new GenericTagQueryFilter<>(new GenericTagQuery("#a", "17375:" + pubkey))
        ));

        ReqMessage req = new ReqMessage("test-sub-id", List.of(filter1, filter2));

        String reqJson = req.encode();
        System.out.println("REQ Message JSON:");
        System.out.println(reqJson);

        // Also test single filter encoding
        String filter1Json = new FiltersEncoder(filter1).encode();
        System.out.println("\nFilter 1 encoded separately:");
        System.out.println(filter1Json);

        String filter2Json = new FiltersEncoder(filter2).encode();
        System.out.println("\nFilter 2 encoded separately:");
        System.out.println(filter2Json);

        assertNotNull(reqJson);
        assertNotNull(filter1Json);
        assertNotNull(filter2Json);
    }
}
