
package nostr.json.values;

import nostr.base.INostrList;
import nostr.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 *
 * @author squirrel
 */
@Data
public class JsonValueList implements JsonValue, INostrList<JsonValue> {

    private final List<JsonValue> list;

    public JsonValueList() {
        this.list = new ArrayList<>();
    }

    public JsonValueList(List<JsonValue> list) {
        this.list = list;
    }

    @Override
    public void add(JsonValue value) {
        this.list.add(value);
    }

    @Override
    public void addAll(INostrList<JsonValue> list) {
        for(JsonValue v : list.getList()) {
            this.list.add(v);
        }
    }

    @Override
    public int size() {
        return this.list.size();
    }
    
    @Override
    public List<JsonValue> getValue() {
        return this.list;
    }
    
    @Override
    public List<JsonValue> getList() {
        return this.list;
    }
}
