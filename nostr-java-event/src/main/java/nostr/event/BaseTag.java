
package nostr.event;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import nostr.base.IEvent;
import nostr.base.ITag;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@JsonPropertyOrder({"code"})
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@Log
public abstract class BaseTag implements ITag {

	@JsonIgnore
    private IEvent parent;

    @Override
    public void setParent(IEvent event) {
        this.parent = event;
    }

    @Override
    public String getCode() {
        var tag = this.getClass().getAnnotation(Tag.class);
        return tag.code();
    }

    @Override
    public String printAttributes(Relay relay, boolean escape) throws NostrException {

        var fields = this.getClass().getDeclaredFields();
        var index = 0;
        var result = new StringBuilder();
        var fieldList = getSupportedFields(fields, relay);

        if (fieldList.size() >= 1) {
            result.append(",");

            for (Field f : fieldList) {
                final String fieldValue = getFieldValue(f);

                if (!escape) {
                    result.append("\"");
                } else {
                    result.append("\\\"");
                }

                result.append(fieldValue);

                if (!escape) {
                    result.append("\"");
                } else {
                    result.append("\\\"");
                }

                if (++index < fieldList.size()) {
                    result.append(",");
                }
            }
        }
        
        return result.toString();
    }

    private List<Field> getSupportedFields(Field[] fields, Relay relay) throws NostrException {
        List<Field> fieldList = new ArrayList<>();
        for (Field f : fields) {
            if (nipSupport(f, relay) && null != getFieldValue(f)) {
                fieldList.add(f);
            }
        }

        return fieldList;
    }

    private boolean nipSupport(Field field, Relay relay) {

        if (field.getAnnotation(Key.class) == null) {
            log.log(Level.FINE, "@Key annotation not found, skipping field {0}", field.getName());
            return false;
        }

        if (relay == null) {
            return true;
        }

        return NipUtil.checkSupport(relay, field);
    }

    private String getFieldValue(Field field) throws NostrException {
        try {
            Object f = new PropertyDescriptor(field.getName(), this.getClass()).getReadMethod().invoke(this);
            return f != null ? f.toString() : null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException ex) {
            throw new NostrException(ex);
        }
    }
}
