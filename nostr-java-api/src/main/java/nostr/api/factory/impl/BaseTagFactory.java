package nostr.api.factory.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.json.codec.EventEncodingException;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility to create {@link BaseTag} instances from code and parameters or from JSON.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseTagFactory {

  private final String code;
  private final List<String> params;

  private String jsonString;

  protected BaseTagFactory() {
    this.code = "";
    this.params = new ArrayList<>();
  }

  /**
   * Initialize with a tag code and params.
   */
  public BaseTagFactory(@NonNull String code, @NonNull List<String> params) {
    this.code = code;
    this.params = params;
  }

  /** Initialize with a tag code and varargs params. */
  public BaseTagFactory(String code, String... params) {
    this(code, Stream.of(params).filter(param -> param != null).toList());
  }

  /** Initialize from a JSON string representing a serialized tag. */
  public BaseTagFactory(@NonNull String jsonString) {
    this.jsonString = jsonString;
    this.code = "";
    this.params = new ArrayList<>();
  }

  /**
   * Build the tag instance based on the factory configuration.
   *
   * <p>If a JSON payload was supplied, it is decoded into a {@link GenericTag}. Otherwise, a tag
   * is built from the configured code and parameters.
   *
   * @return the constructed tag instance
   * @throws EventEncodingException if the JSON payload cannot be parsed
   */
  public BaseTag create() {
    if (jsonString != null) {
      try {
        return new ObjectMapper().readValue(jsonString, GenericTag.class);
      } catch (JsonProcessingException ex) {
        throw new EventEncodingException("Failed to decode tag from JSON", ex);
      }
    }
    return BaseTag.create(code, params);
  }
}
