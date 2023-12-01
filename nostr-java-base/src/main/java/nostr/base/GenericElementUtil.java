package nostr.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class GenericElementUtil {

    @NonNull
    private final IGenericElement genericElement;

    public ElementAttribute getAttribute(int index) {
        var attributes = this.genericElement.getAttributes();
        if (index < 0 || index >= attributes.size()) {
            throw new IndexOutOfBoundsException("Index is out of bounds");
        }

        List<ElementAttribute> list = new ArrayList<>(attributes);
        return list.get(index);
    }

    public Object getAttributeValue(int index) {
        var attribute = getAttribute(index);
        return attribute.getValue();
    }

    public String getAttributeName(int index) {
        var attribute = getAttribute(index);
        return attribute.getName();
    }
}
