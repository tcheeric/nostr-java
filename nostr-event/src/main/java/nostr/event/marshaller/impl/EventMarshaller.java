package nostr.event.marshaller.impl;

import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.Relay;
import nostr.util.UnsupportedNIPException;
import com.tcheeric.nostr.base.annotation.JsonList;
import com.tcheeric.nostr.base.annotation.JsonString;
import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import nostr.event.marshaller.BaseMarshaller;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@SuppressWarnings("Lombok")
@Data
@EqualsAndHashCode(callSuper = false)
@Log
public class EventMarshaller extends BaseMarshaller {

    private boolean escape;

    public EventMarshaller(IEvent event, Relay relay) {
        this(event, relay, false);
    }

    public EventMarshaller(IEvent event, Relay relay, boolean escape) {
        super(event, relay);
        this.escape = escape;
    }

    @Override
    public String marshall() throws UnsupportedNIPException {

        Relay relay = getRelay();

        if (!nipEventSupport()) {
            final int value = getNip() != null ? getNip().value() : 1;
            throw new UnsupportedNIPException("NIP " + value + " is not supported by relay: \"" + relay.getName() + "\"  - List of supported NIP(s): " + relay.printSupportedNips());
        }

        try {
            Map<Field, Object> keysMapEvent = getKeysMap();

            StringBuilder jsonEvent = toJson(keysMapEvent);

            return "{" + jsonEvent + "}";
        } catch (IllegalArgumentException | NoSuchAlgorithmException | IllegalAccessException | IntrospectionException | InvocationTargetException | NostrException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    private StringBuilder toJson(Map<Field, Object> keysMap) throws NostrException {
        int i = 0;
        var result = new StringBuilder();
        Relay relay = getRelay();

        for (var field : keysMap.keySet()) {
            toJson(field, result, keysMap, relay, ++i);
        }

        return result;
    }

    protected void toJson(Field field, StringBuilder result, Map<Field, Object> keysMap, Relay relay, int i) throws NostrException {
        final String fieldName = getFieldName(field);

        if (!escape) {
            result.append("\"");
        } else {
            result.append("\\\"");
        }
        result.append(fieldName);
        if (!escape) {
            result.append("\":");
        } else {
            result.append("\\\":");
        }

        final boolean quoteFlag = isStringType(field);

        if (quoteFlag) {
            if (!escape) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }
        }

        final Object value = keysMap.get(field);
        if (value != null) {
            final String strValue = value instanceof IElement ? new BaseMarshaller.Factory((IElement) value).create(relay, escape).marshall() : value.toString();
            result.append(strValue);
        }

        if (quoteFlag) {
            if (!escape) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }
        }

        if (i < keysMap.size()) {
            result.append(",");
        }
    }

    private Map<Field, Object> getKeysMap() throws IllegalArgumentException, NoSuchAlgorithmException, IllegalAccessException, IntrospectionException, InvocationTargetException {

        IEvent event = (IEvent) getElement();
        Relay relay = getRelay();

        Map<Field, Object> keysMap = new HashMap<>();

        Field[] fieldArr = getFields();

        for (Field field : fieldArr) {

            if (!nipFieldSupport(field)) {
                log.log(Level.FINE, "Relay {0} to ignore field {1}", new Object[]{relay, field});
                continue;
            }

            field.setAccessible(true);

            if (field.isAnnotationPresent(Key.class)) {

                final String fieldName = getFieldName(field);
                switch (fieldName) {
                    case "id" ->
                        keysMap.put(field, getValue(field));
                    case "pubkey" ->
                        keysMap.put(field, getValue(field));
                    case "sig" ->
                        keysMap.put(field, getValue(field));
                    default -> {
                        if (field.get(event) != null) {
                            keysMap.put(field, field.get(event));
                        }
                    }
                }
            }
        }

        return keysMap;
    }

    @NonNull
    protected String getFieldName(Field field) {
        String name = field.getAnnotation(Key.class).name();
        return name.isEmpty() ? field.getName() : name;
    }

    @SuppressWarnings("unchecked")
    private boolean isStringType(Field field) throws NostrException {
        try {
            Class<?> clazz = field.getType();

            if (clazz.isAnnotationPresent(JsonList.class)) {
                return false;
            }

            return clazz.equals(String.class) || field.isAnnotationPresent(JsonString.class) /*&& !isCompositeType(getValue(field))*/;

        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }
    }

    private Object getValue(Field field) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        IEvent event = (IEvent) getElement();

        String attribute = field.getName();

        return new PropertyDescriptor(attribute, event.getClass()).getReadMethod().invoke(event);
    }

    private Field[] getFields() {

        IEvent event = (IEvent) getElement();
        final Class<? extends IEvent> clazz = event.getClass();

        Field[] superFields = clazz.getSuperclass().getDeclaredFields();
        Field[] thisFields = clazz.getDeclaredFields();
        Field[] fieldArr = Arrays.copyOf(superFields, superFields.length + thisFields.length);
        System.arraycopy(thisFields, 0, fieldArr, superFields.length, thisFields.length);

        return fieldArr;
    }

    private boolean nipEventSupport() {

        IEvent event = (IEvent) getElement();
        Relay relay = getRelay();

        if (relay == null) {
            return true;
        }

        List<Integer> snips = relay.getSupportedNips();

        NIPSupport n = getNip();
        var nip = n != null ? n.value() : 1;
        return snips.contains(nip);
    }

    private NIPSupport getNip() {
        IEvent event = (IEvent) getElement();
        var n = event.getClass().getAnnotation(NIPSupport.class);
        return n;
    }
}
