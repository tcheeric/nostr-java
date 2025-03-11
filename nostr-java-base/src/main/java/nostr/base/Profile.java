package nostr.base;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.net.URL;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class Profile {

  private final String name;

  @ToString.Exclude
  private String about;

  @ToString.Exclude
  private URL picture;

  protected Profile() {
    this.name = null;
  }

  @JsonValue
  @Override
  public String toString() {
    try {
      return MAPPER_AFTERBURNER.writeValueAsString(this);
    } catch (JsonProcessingException ex) {
      throw new RuntimeException(ex);
    }
  }

}
