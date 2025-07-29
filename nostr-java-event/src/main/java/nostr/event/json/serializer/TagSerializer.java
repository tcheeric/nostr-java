package nostr.event.json.serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseTag;
import nostr.event.tag.GenericTag;

import java.io.Serial;


/**
 * @author guilhermegps
 */
@Slf4j
public class TagSerializer extends AbstractTagSerializer<BaseTag> {

    @Serial
    private static final long serialVersionUID = -3877972991082754068L;

    public TagSerializer() {
        super(BaseTag.class);
    }

    @Override
    protected void applyCustomAttributes(ObjectNode node, BaseTag value) {
        if (value instanceof GenericTag genericTag) {
            genericTag.getAttributes()
                    .forEach(a -> node.put(a.getName(), a.getValue().toString()));
        }
    }


}
