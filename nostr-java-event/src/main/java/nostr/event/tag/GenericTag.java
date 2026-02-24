package nostr.event.tag;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.json.serializer.GenericTagSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = GenericTagSerializer.class)
public class GenericTag extends BaseTag {

  private String code;

  private final List<String> params;

  public GenericTag() {
    this("");
  }

  public GenericTag(@NonNull String code) {
    this(code, new ArrayList<>());
  }

  public GenericTag(@NonNull String code, @NonNull List<String> params) {
    this.code = code;
    this.params = new ArrayList<>(params);
  }

  public static GenericTag of(@NonNull String code, @NonNull String... params) {
    return new GenericTag(code, List.of(params));
  }

  @Override
  public String getCode() {
    return "".equals(this.code) ? super.getCode() : this.code;
  }

  public List<String> getParams() {
    return Collections.unmodifiableList(this.params);
  }

  public void addParam(@NonNull String param) {
    this.params.add(param);
  }

  public void addParams(@NonNull List<String> params) {
    this.params.addAll(params);
  }

  public String[] toArray() {
    List<String> all = new ArrayList<>();
    all.add(code);
    all.addAll(params);
    return all.toArray(new String[0]);
  }
}
