package nostr.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import nostr.event.json.deserializer.TagDeserializer;
import nostr.event.json.serializer.BaseTagSerializer;
import nostr.event.tag.GenericTag;

import java.util.List;

@Data
@ToString
@EqualsAndHashCode
@JsonDeserialize(using = TagDeserializer.class)
@JsonSerialize(using = BaseTagSerializer.class)
public abstract class BaseTag {

  public String getCode() {
    return "";
  }

  public static BaseTag create(@NonNull String code, @NonNull String... params) {
    return create(code, List.of(params));
  }

  public static BaseTag create(@NonNull String code, @NonNull List<String> params) {
    return new GenericTag(code, params);
  }
}
