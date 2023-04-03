package nostr.event.marshaller.impl;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.ElementAttribute;
import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.NipUtil;
import nostr.base.Relay;
import nostr.base.annotation.JsonList;
import nostr.base.annotation.JsonString;
import nostr.base.annotation.Key;
import nostr.event.impl.GenericEvent;
import nostr.event.marshaller.BaseElementMarshaller;
import nostr.types.MarshallException;
import nostr.types.values.marshaller.BaseTypesMarshaller;
import nostr.util.NostrException;
import nostr.util.UnsupportedNIPException;

/**
 *
 * @author squirrel
 */
@SuppressWarnings("Lombok")
@Data
@EqualsAndHashCode(callSuper = false)
@Log
public class EventMarshaller extends BaseElementMarshaller {

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
            throw new UnsupportedNIPException("NIP is not supported by relay: \"" + relay.getName() + "\"  - List of supported NIP(s): " + relay.printSupportedNips());
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

        final boolean isString = isStringType(field);

        if (isString) {
            if (!escape) {
                result.append("\"");
            } else {
                result.append("\\\"");
            }
        }

        final Object value = keysMap.get(field);
        if (value != null) {
            final String strValue = value instanceof IElement ? new BaseElementMarshaller.Factory((IElement) value).create(relay, escape).marshall() : value.toString();
            result.append(strValue);
        }

        if (isString) {
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

    private StringBuilder toJson(Map<Field, Object> keysMap) throws NostrException {
        int i = 0;
        var result = new StringBuilder();
        Relay relay = getRelay();

        // Process the base attributes
        for (var field : keysMap.keySet()) {
            toJson(field, result, keysMap, relay, ++i);
        }

        // Process custom attributes
        Set<ElementAttribute> attrs = getSupportedAttributes(relay);
        if (!attrs.isEmpty()) {
            i = 0;
            result.append(",");

            for (var a : attrs) {
                toJson(a, result, attrs, ++i);
            }
        }

        return result;
    }

    private void toJson(ElementAttribute attribute, StringBuilder result, Set<ElementAttribute> attrs, int i) throws NostrException {

        try {
            result.append(BaseTypesMarshaller.Factory.create(attribute.getValue(), escape).marshall());
        } catch (MarshallException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }

        if (i < attrs.size()) {
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
            } else {
                log.log(Level.INFO, "Ignoring field {0}", field.getName());
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

        Class clazz = event.getClass();
        Field[] thisFields = clazz.getDeclaredFields();
        Field[] fieldArr = Arrays.copyOf(thisFields, thisFields.length);

        clazz = clazz.getSuperclass();
        while (!clazz.equals(Object.class)) {
            thisFields = clazz.getDeclaredFields();

            var tmp = Arrays.copyOf(thisFields, thisFields.length + fieldArr.length);
            System.arraycopy(fieldArr, 0, tmp, thisFields.length, fieldArr.length);
            fieldArr = tmp;

            clazz = clazz.getSuperclass();
        }
        return fieldArr;
    }

    private boolean nipEventSupport() {

        Relay relay = getRelay();

        if (relay == null) {
            return true;
        }

        IEvent event = (IEvent) getElement();
        return NipUtil.checkSupport(relay, event);
    }

    private Set<ElementAttribute> getSupportedAttributes(Relay relay) {
        IEvent event = (IEvent) getElement();
        Set<ElementAttribute> result = new HashSet<>();

        if (event instanceof GenericEvent genericEvent) {
            Set<ElementAttribute> attrs = genericEvent.getAttributes();
            for (var a : attrs) {
                if (relay != null && relay.getSupportedNips().contains(a.getNip())) {
                    result.add(a);
                }
            }
        }

        return result;
    }
}
