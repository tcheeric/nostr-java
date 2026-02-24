package nostr.event.unit;

import nostr.base.Kinds;
import nostr.event.filter.EventFilter;
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
        EventFilter filter = EventFilter.builder()
            .kind(Kinds.WALLET)
            .kind(Kinds.WALLET_UNSPENT_PROOF)
            .kind(Kinds.WALLET_TX_HISTORY)
            .author(TEST_PUBKEY)
            .build();

        String filterJson = new FiltersEncoder(filter).encode();

        assertNotNull(filterJson);
        assertTrue(filterJson.contains("17375"), "Should contain WALLET kind (17375)");
        assertTrue(filterJson.contains("7375"), "Should contain WALLET_UNSPENT_PROOF kind (7375)");
        assertTrue(filterJson.contains("7376"), "Should contain WALLET_TX_HISTORY kind (7376)");
        assertTrue(filterJson.contains(TEST_PUBKEY), "Should contain author pubkey");
        assertTrue(filterJson.contains("\"kinds\""), "Should have 'kinds' field");
        assertTrue(filterJson.contains("\"authors\""), "Should have 'authors' field");
    }

    @Test
    void testNip60ProofFilterWithTagQuery() {
        String walletRef = Kinds.WALLET + ":" + TEST_PUBKEY;
        EventFilter filter = EventFilter.builder()
            .kind(Kinds.WALLET_UNSPENT_PROOF)
            .addTagFilter("a", walletRef)
            .build();

        String filterJson = new FiltersEncoder(filter).encode();

        assertNotNull(filterJson);
        assertTrue(filterJson.contains("7375"), "Should contain WALLET_UNSPENT_PROOF kind");
        assertTrue(filterJson.contains("\"#a\""), "Should have '#a' tag filter");
        assertTrue(filterJson.contains(walletRef), "Should contain wallet reference in #a tag");
    }

    @Test
    void testNip60ReqMessageFormat() {
        EventFilter walletFilter = EventFilter.builder()
            .kind(Kinds.WALLET)
            .author(TEST_PUBKEY)
            .build();

        EventFilter proofFilter = EventFilter.builder()
            .kind(Kinds.WALLET_UNSPENT_PROOF)
            .author(TEST_PUBKEY)
            .build();

        ReqMessage req = new ReqMessage("nip60-sync", List.of(walletFilter, proofFilter));
        String reqJson = req.encode();

        assertNotNull(reqJson);
        assertTrue(reqJson.startsWith("[\"REQ\""), "Should start with REQ command");
        assertTrue(reqJson.contains("\"nip60-sync\""), "Should contain subscription ID");
        assertTrue(reqJson.contains("17375"), "Should contain WALLET kind");
        assertTrue(reqJson.contains("7375"), "Should contain WALLET_UNSPENT_PROOF kind");
    }
}
