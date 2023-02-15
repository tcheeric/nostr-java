package nostr.event.unmarshaller.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IUnmarshaller;
import nostr.event.list.TagList;
import nostr.json.unmarshaller.impl.JsonArrayUnmarshaller;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
public class TagListUnmarshaller implements IUnmarshaller<TagList> {

    private final String json;
    private final boolean escape;

    public TagListUnmarshaller(String tagList) {
        this(tagList, false);
    }

    @Override
    public TagList unmarshall() {
        var value = new JsonArrayUnmarshaller(this.getJson()).unmarshall();
        TagList result = new TagList();

        for (int i = 0; i < value.length(); i++) {
            var tag = value.get(i).get();
            result.add(new TagUnmarshaller(tag.toString()).unmarshall());
        }
        
        return result;
    }

}
