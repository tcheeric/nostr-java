package nostr.event.unmarshaller.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import nostr.base.GenericTagQuery;
import nostr.base.IElement;
import nostr.base.PublicKey;
import nostr.event.BaseEvent.ProxyEvent;
import nostr.event.Kind;
import nostr.event.impl.Filters;
import nostr.event.list.EventList;
import nostr.event.list.GenericTagQueryList;
import nostr.event.list.KindList;
import nostr.event.list.PublicKeyList;
import nostr.event.unmarshaller.BaseElementUnmarshaller;
import nostr.json.unmarshaller.impl.JsonObjectUnmarshaller;
import nostr.types.values.impl.ArrayValue;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.NumberValue;
import nostr.util.NostrUtil;

/**
 *
 * @author squirrel
 */
public class FiltersUnmarshaller extends BaseElementUnmarshaller {

    public FiltersUnmarshaller(String event) {
        this(event, false);
    }

    public FiltersUnmarshaller(String event, boolean escape) {
        super(event, escape);
    }

    @Override
    public IElement unmarshall() {

        var value = new JsonObjectUnmarshaller(getJson()).unmarshall();
        var publicKeyList = new PublicKeyList();
        var refPublicKeyList = new PublicKeyList();
        var events = new EventList();
        var referencedEvents = new EventList();
        var kindList = new KindList();
        final Optional<GenericTagQueryList> optGtql = getGenericTagQueryList();
        var genericTagQueryList = optGtql.isEmpty() ? new GenericTagQueryList() : optGtql.get();
        ArrayValue arr;

        // Authors
        var optArr = value.get("authors");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                String hex = arr.get(i).get().getValue().toString();
                publicKeyList.add(new PublicKey(NostrUtil.hexToBytes(hex)));
            }
        }

        // Ref. pub keys
        optArr = value.get("#p");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                String hex = arr.get(i).get().getValue().toString();
                refPublicKeyList.add(new PublicKey(NostrUtil.hexToBytes(hex)));
            }
        }

        //  Events        
        optArr = value.get("ids");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                String eventId = arr.get(i).get().getValue().toString();
                events.add(new ProxyEvent(eventId));
            }
        }

        // Referenced Events        
        optArr = value.get("#e");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            referencedEvents = new EventList();
            for (var i = 0; i < arr.length(); i++) {
                String eventId = arr.get(i).get().getValue().toString();
                referencedEvents.add(new ProxyEvent(eventId));
            }
        }

        // Kinds
        optArr = value.get("kinds");
        if (!optArr.isEmpty()) {
            arr = (ArrayValue) optArr.get();
            for (var i = 0; i < arr.length(); i++) {
                var ikind = ((NumberValue) arr.get(i).get()).intValue();
                Kind kind = Kind.valueOf(ikind);
                kindList.add(kind);
            }
        }

        //Limit
        var optNumber = value.get("limit");
        Integer limit = optNumber.isEmpty() ? null : ((NumberValue) optNumber.get()).intValue();

        // Since
        optNumber = value.get("since");
        var since = optNumber.isEmpty() ? null : ((NumberValue) optNumber.get()).longValue();

        // Since
        optNumber = value.get("until");
        var until = optNumber.isEmpty() ? null : ((NumberValue) optNumber.get()).longValue();

        // Generic TagList 
        Filters filters = Filters.builder()
                .authors(publicKeyList)
                .events(events)
                .kinds(kindList).limit(limit)
                .since(since)
                .referencedEvents(referencedEvents)
                .referencePubKeys(publicKeyList)
                .genericTagQueryList(genericTagQueryList)
                .until(until)
                .build();

        return filters;
    }

    private Optional<GenericTagQueryList> getGenericTagQueryList() {

        var obj = new JsonObjectUnmarshaller(getJson()).unmarshall();
        var exprList = (List<ExpressionValue>) obj.getValue();
        final Optional<GenericTagQueryList> findAny = exprList.stream()
                .filter(e -> !Arrays.asList("authors", "ids", "since", "until", "limit", "kinds", "#e", "#p").contains(e.getName()))
                .map(FiltersUnmarshaller::gtql).findAny();
        return findAny.isEmpty() ? Optional.empty() : findAny;

    }

    private static GenericTagQueryList gtql(ExpressionValue e) {
        GenericTagQueryList result = new GenericTagQueryList();
        var tagName = e.getName().charAt(1);
        var valueList = new ArrayList<String>();

        final ArrayValue attrArr = (ArrayValue) e.getValue();
        for (var i = 0; i < attrArr.length(); i++) {
            var s = attrArr.get(i).get().getValue().toString();
            valueList.add(s);
        }
        var gtq = GenericTagQuery.builder().tagName(tagName).value(valueList).build();
        result.add(gtq);
        return result;
    }
}
