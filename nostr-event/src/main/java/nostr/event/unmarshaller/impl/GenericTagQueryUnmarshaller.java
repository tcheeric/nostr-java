package nostr.event.unmarshaller.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.GenericTagQuery;
import nostr.base.IUnmarshaller;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
public class GenericTagQueryUnmarshaller implements IUnmarshaller<GenericTagQuery> {

    private final String json;

    @Override
    public GenericTagQuery unmarshall() {

        var value = new JsonArrayUnmarshaller(json).unmarshall();

        Character tagName = value.get(0).get().getValue().toString().charAt(0);
        List<String> valueList = new ArrayList<>();
        for (var i = 0; i < value.length(); i++) {
            valueList.add(value.get(i).get().getValue().toString());
        }

        return GenericTagQuery.builder().tagName(tagName).value(valueList).build();
    }

}
