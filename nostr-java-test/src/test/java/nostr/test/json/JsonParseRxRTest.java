package nostr.test.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.java.Log;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.filter.FiltersCore;
import nostr.event.filter.KindFilter;
import nostr.event.filter.PublicKeyFilter;
import nostr.event.filter.ReferencedEventFilter;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.ReqMessageRxR;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log
public class JsonParseRxRTest {
  @Test
  public void testReqMessagePopulatedListOfFiltersListDecoder() throws JsonProcessingException {
    log.info("testReqMessagePopulatedListOfFiltersListDecoder");

    String subscriptionId = "npub17x6pn22ukq3n5yw5x9prksdyyu6ww9jle2ckpqwdprh3ey8qhe6stnpujh";
    String kind = "1";
    String author = "f1b419a95cb0233a11d431423b41a42734e7165fcab16081cd08ef1c90e0be75";
    String geohashKey = "#g";
    String geohashValue1 = "2vghde";
    String geohashValue2 = "3abcde";
    String referencedEventId = "fc7f200c5bed175702bd06c7ca5dba90d3497e827350b42fc99c3a4fa276a712";
    String uuidKey = "#d";
    String uuidValue1 = "UUID-1";
    String uuidValue2 = "UUID-2";
    String reqJsonWithCustomTagQueryFilterToDecode =
        "[\"REQ\", " +
            "\"" + subscriptionId + "\", " +
            "{\"kinds\": [" + kind + "], " +
            "\"authors\": [\"" + author + "\"]," +
//            "\"" + geohashKey + "\": [\"" + geohashValue1 + "\",\"" + geohashValue2 + "\"]," +
//            "\"" + uuidKey + "\": [\"" + uuidValue1 + "\",\"" + uuidValue2 + "\"]," +
            "\"#e\": [\"" + referencedEventId + "\"]}]";

    ReqMessageRxR decodedReqMessage = new BaseMessageDecoder<ReqMessageRxR>().decode(reqJsonWithCustomTagQueryFilterToDecode);

    FiltersCore expectedFilters = new FiltersCore();
    expectedFilters.addFilterable(KindFilter.filterKey, List.of(new KindFilter<>(Kind.TEXT_NOTE)));
    expectedFilters.addFilterable(PublicKeyFilter.filterKey, List.of(new PublicKeyFilter<>(new PublicKey(author))));
    expectedFilters.addFilterable(ReferencedEventFilter.filterKey, List.of(new ReferencedEventFilter<>(new GenericEvent(referencedEventId))));
//    List<String> expectedGeohashValuesList = List.of(geohashValue1, geohashValue2);
//    expectedFilters.setGenericTagQuery(geohashKey, expectedGeohashValuesList);
//    List<String> expectedIdentityTagValuesList = List.of(uuidValue1, uuidValue2);
//    expectedFilters.setGenericTagQuery(uuidKey, expectedIdentityTagValuesList);
    ReqMessageRxR expectedReqMessage = new ReqMessageRxR(subscriptionId, expectedFilters.getFiltersMap());
    String expected = expectedReqMessage.encode();
    String actual = decodedReqMessage.encode();
    assertEquals(expected, actual);
    assertEquals(expectedReqMessage, decodedReqMessage);
  }
}
