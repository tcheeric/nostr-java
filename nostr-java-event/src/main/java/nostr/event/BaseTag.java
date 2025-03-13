package nostr.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nostr.base.IEvent;
import nostr.base.ITag;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.json.deserializer.TagDeserializer;
import nostr.event.json.serializer.TagSerializer;
import nostr.util.NostrException;
import org.apache.commons.lang3.stream.Streams;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author squirrel
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@JsonDeserialize(using = TagDeserializer.class)
@JsonSerialize(using = TagSerializer.class)
public abstract class BaseTag implements ITag {

    @JsonIgnore
    private IEvent parent;

    @Override
    public void setParent(IEvent event) {
        this.parent = event;
    }

    @Override
    public String getCode() {
        return this.getClass().getAnnotation(Tag.class).code();
    }

    @Override
    public Integer getNip() {
        return 1;
    }

    public String getFieldValue(Field field) throws NostrException {
        try {
            Object f = new PropertyDescriptor(field.getName(), this.getClass()).getReadMethod().invoke(this);
            return f != null ? f.toString() : null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException ex) {
            throw new NostrException(ex);
        }
    }

    public List<Field> getSupportedFields() throws NostrException {
        return new Streams.FailableStream<>(Arrays.stream(this.getClass().getDeclaredFields()))
                .filter(f ->
                        Objects.nonNull(f.getAnnotation(Key.class)))
                .filter(f ->
                        Objects.nonNull(getFieldValue(f)))
                .collect(Collectors.toList());
    }

    protected static <T extends BaseTag> void setOptionalField(JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
        Optional.ofNullable(node).ifPresent(n -> con.accept(n, tag));
    }

    protected static <T extends BaseTag> void setRequiredField(JsonNode node, BiConsumer<JsonNode, T> con, T tag) {
        con.accept(Optional.ofNullable(node).orElseThrow(), tag);
    }
}
