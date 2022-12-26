
package nostr.event.marshaller.impl;

import nostr.base.ITag;
import nostr.base.Relay;
import nostr.util.UnsupportedNIPException;
import nostr.base.annotation.NIPSupport;
import nostr.event.BaseTag;
import nostr.event.marshaller.BaseMarshaller;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@SuppressWarnings("Lombok")
@Data
@Log
@EqualsAndHashCode(callSuper = false)
public class TagMarshaller extends BaseMarshaller {

    public TagMarshaller(ITag tag, Relay relay) {
        this(tag, relay, false);
    }

    public TagMarshaller(ITag iTag, Relay relay, boolean escape) {
        super(iTag, relay, escape);
    }

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }

    private String toJson() throws NostrException {

        ITag tag = (ITag) getElement();
        Relay relay = getRelay();

        if (relay != null && !nipSupportForTag()) {
            throw new UnsupportedNIPException(relay + "does not support tag " + tag.getCode());
        }

        StringBuilder result = new StringBuilder();
        result.append("[");
        if (!isEscape()) {
            result.append("\"");
        } else {
            result.append("\\\"");
        }
        
        result.append(tag.getCode());
        
        if (!isEscape()) {
            result.append("\",");
        } else {
            result.append("\\\",");
        }
        
        result.append(printAttributes());
        result.append("]");

        return result.toString();
    }

    private String printAttributes() throws NostrException {

        var tag = (ITag) getElement();
        var fields = tag.getClass().getDeclaredFields();
        var index = 0;
        var result = new StringBuilder();
        var fieldList = getSupportedFields(fields);

        for (Field f : fieldList) {
            final String fieldValue = getFieldValue(f);
            
            if (!isEscape()) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }

            result.append(fieldValue);

            if (!isEscape()) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }

            if (++index < fieldList.size()) {
                result.append(",");
            }
        }

        return result.toString();
    }

    private List<Field> getSupportedFields(Field[] fields) throws NostrException {
        List<Field> fieldList = new ArrayList<>();
        for (Field f : fields) {
            if (nipFieldSupport(f) && null != getFieldValue(f)) {
                fieldList.add(f);
            }
        }

        return fieldList;
    }

    private boolean nipSupportForTag() {

        Relay relay = getRelay();
        List<Integer> snips = relay.getSupportedNips();
        ITag tag = (ITag) getElement();

        var n = this.getClass().getAnnotation(NIPSupport.class);
        var nip = n != null ? n.value() : 1;

        if (!snips.contains(nip)) {
            return false;
        }

        if (((BaseTag) tag).getParent() != null) {
            n = ((BaseTag) tag).getParent().getClass().getAnnotation(NIPSupport.class);
            nip = n != null ? n.value() : 1;
            return snips.contains(nip);
        }

        return true;
    }

    private String getFieldValue(Field field) throws NostrException {
        try {
            var tag = (ITag) getElement();
            Object f = new PropertyDescriptor(field.getName(), tag.getClass()).getReadMethod().invoke(tag);
            return f != null ? f.toString() : null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException ex) {
            log.log(Level.WARNING, null, ex);
            throw new NostrException(ex);
        }
    }

}
