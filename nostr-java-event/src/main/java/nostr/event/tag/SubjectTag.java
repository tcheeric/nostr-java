package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.util.Optional;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/** Represents a 'subject' tag (NIP-14). */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Tag(code = "subject", nip = 14)
@JsonPropertyOrder({"subject"})
public final class SubjectTag extends BaseTag {

  @Key
  @JsonProperty("subject")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String subject;

  /** Optional accessor for subject. */
  public Optional<String> getSubjectOptional() {
    return Optional.ofNullable(subject);
  }

  public static SubjectTag deserialize(@NonNull JsonNode node) {
    SubjectTag tag = new SubjectTag();
    setOptionalField(node.get(1), (n, t) -> tag.setSubject(n.asText()), tag);
    return tag;
  }

  public static SubjectTag updateFields(@NonNull GenericTag genericTag) {
    if (!"subject".equals(genericTag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for SubjectTag");
    }

    SubjectTag subjectTag = new SubjectTag(genericTag.getAttributes().get(0).value().toString());
    return subjectTag;
  }
}
