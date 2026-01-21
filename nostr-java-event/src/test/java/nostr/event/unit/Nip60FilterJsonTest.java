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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to verify NIP-60 filter JSON serialization format.
 */
class Nip60FilterJsonTest {

    private static final String TEST_PUBKEY = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";

    @Test
    void testNip60WalletFilterJsonFormat() {
        // First filter: wallet-related kinds + author
        Filters filter = new Filters(List.of(
            new KindFilter<>(Kind.WALLET),
            new KindFilter<>(Kind.WALLET_UNSPENT_PROOF),
            new KindFilter<>(Kind.WALLET_TX_HISTORY),
            new AuthorFilter<>(new PublicKey(TEST_PUBKEY))
        ));

        String filterJson = new FiltersEncoder(filter).encode();

        assertNotNull(filterJson);
        // Verify kinds are present with correct NIP-60 values
        assertTrue(filterJson.contains("17375"), "Should contain WALLET kind (17375)");
        assertTrue(filterJson.contains("7375"), "Should contain WALLET_UNSPENT_PROOF kind (7375)");
        assertTrue(filterJson.contains("7376"), "Should contain WALLET_TX_HISTORY kind (7376)");
        // Verify author pubkey is present
        assertTrue(filterJson.contains(TEST_PUBKEY), "Should contain author pubkey");
        // Verify JSON structure has expected fields
        assertTrue(filterJson.contains("\"kinds\""), "Should have 'kinds' field");
        assertTrue(filterJson.contains("\"authors\""), "Should have 'authors' field");
    }

    @Test
    void testNip60ProofFilterWithTagQuery() {
        // Filter with kind + #a tag query (wallet proof lookup by wallet reference)
        String walletRef = Kind.WALLET.getValue() + ":" + TEST_PUBKEY;
        Filters filter = new Filters(List.of(
            new KindFilter<>(Kind.WALLET_UNSPENT_PROOF),
            new GenericTagQueryFilter<>(new GenericTagQuery("#a", walletRef))
        ));

        String filterJson = new FiltersEncoder(filter).encode();

        assertNotNull(filterJson);
        assertTrue(filterJson.contains("7375"), "Should contain WALLET_UNSPENT_PROOF kind");
        assertTrue(filterJson.contains("\"#a\""), "Should have '#a' tag filter");
        assertTrue(filterJson.contains(walletRef), "Should contain wallet reference in #a tag");
    }

    @Test
    void testNip60ReqMessageFormat() {
        Filters walletFilter = new Filters(List.of(
            new KindFilter<>(Kind.WALLET),
            new AuthorFilter<>(new PublicKey(TEST_PUBKEY))
        ));

        Filters proofFilter = new Filters(List.of(
            new KindFilter<>(Kind.WALLET_UNSPENT_PROOF),
            new AuthorFilter<>(new PublicKey(TEST_PUBKEY))
        ));

        ReqMessage req = new ReqMessage("nip60-sync", List.of(walletFilter, proofFilter));
        String reqJson = req.encode();

        assertNotNull(reqJson);
        // REQ message format: ["REQ", <subscription_id>, <filter1>, <filter2>, ...]
        assertTrue(reqJson.startsWith("[\"REQ\""), "Should start with REQ command");
        assertTrue(reqJson.contains("\"nip60-sync\""), "Should contain subscription ID");
        assertTrue(reqJson.contains("17375"), "Should contain WALLET kind");
        assertTrue(reqJson.contains("7375"), "Should contain WALLET_UNSPENT_PROOF kind");
    }
}
