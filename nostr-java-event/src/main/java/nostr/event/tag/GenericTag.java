package nostr.event.tag;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;
import nostr.event.json.serializer.GenericTagSerializer;

/**
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = GenericTagSerializer.class)
public class GenericTag extends BaseTag implements IGenericElement {

  private String code;

  private final List<ElementAttribute> attributes;

  public GenericTag() {
    this("");
  }

  public GenericTag(@NonNull String code) {
    this(code, new ArrayList<>());
  }

  /**
   * nip parameter to be removed
   *
   * @deprecated use any available proper constructor variant instead
   */
  @Deprecated(forRemoval = true)
  public GenericTag(String code, Integer nip) {
    this(code, new ArrayList<>());
  }

  public GenericTag(@NonNull String code, @NonNull ElementAttribute... attribute) {
    this(code, List.of(attribute));
  }

  public GenericTag(@NonNull String code, @NonNull List<ElementAttribute> attributes) {
    this.code = code;
    this.attributes = attributes;
  }

  @Override
  public String getCode() {
    return "".equals(this.code) ? super.getCode() : this.code;
  }

  @Override
  public void addAttribute(@NonNull ElementAttribute... attribute) {
    this.addAttributes(List.of(attribute));
  }

  @Override
  public void addAttributes(@NonNull List<ElementAttribute> attributes) {
    this.attributes.addAll(attributes);
  }
}
