/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;

/**
 * @author squirrel
 */
@JsonPropertyOrder({"pubKey", "mainRelayUrl", "petName"})
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "p")
@NoArgsConstructor
public class PubKeyTag extends BaseTag {

  @Key
  @JsonProperty("publicKey")
  private PublicKey publicKey;

  @Key
  @JsonProperty("mainRelayUrl")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String mainRelayUrl;

  @Key(nip = 2)
  @JsonProperty("petName")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String petName;

  public PubKeyTag(@NonNull PublicKey publicKey) {
    this.publicKey = publicKey;
  }

  public PubKeyTag(@NonNull PublicKey publicKey, String mainRelayUrl, String petName) {
    this.publicKey = publicKey;
    this.mainRelayUrl = mainRelayUrl;
    this.petName = petName;
  }

  @SuppressWarnings("unchecked")
  public static <T extends BaseTag> T deserialize(@NonNull JsonNode node) {
    PubKeyTag tag = new PubKeyTag();
    setRequiredField(node.get(1), (n, t) -> tag.setPublicKey(new PublicKey(n.asText())), tag);
    setOptionalField(node.get(2), (n, t) -> tag.setMainRelayUrl(n.asText()), tag);
    setOptionalField(node.get(3), (n, t) -> tag.setPetName(n.asText()), tag);
    return (T) tag;
  }

  public static PubKeyTag updateFields(@NonNull GenericTag tag) {
    if (!"p".equals(tag.getCode())) {
      throw new IllegalArgumentException("Invalid tag code for PubKeyTag");
    }

    PublicKey pubKey = new PublicKey(tag.getAttributes().get(0).value().toString());

    String mainRelayUrl =
        tag.getAttributes().size() > 1 ? tag.getAttributes().get(1).value().toString() : null;
    String petName =
        tag.getAttributes().size() > 2 ? tag.getAttributes().get(2).value().toString() : null;
    PubKeyTag pubKeyTag = new PubKeyTag(pubKey, mainRelayUrl, petName);
    return pubKeyTag;
  }
}
