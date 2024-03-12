package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nostr.base.*;
import nostr.base.annotation.Key;
import nostr.crypto.bech32.Bech32;
import nostr.crypto.bech32.Bech32Prefix;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.json.deserializer.KindDeserializer;
import nostr.event.json.deserializer.PublicKeyDeserializer;
import nostr.event.json.deserializer.SignatureDeserializer;
import nostr.util.NostrException;
import nostr.util.NostrUtil;

import java.beans.Transient;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author squirrel
 */
@Getter
@Setter
@EqualsAndHashCode
public class GenericEventImpl implements GenericEventNick, ISignable, IGenericElement {
  @Key
  @EqualsAndHashCode.Include
  private String id;

  @Key
  @JsonProperty("pubkey")
  @EqualsAndHashCode.Include
  @JsonDeserialize(using = PublicKeyDeserializer.class)
  private PublicKey pubKey;

  @Key
  @JsonProperty("created_at")
  @EqualsAndHashCode.Exclude
  private Long createdAt;

  @Key
  @EqualsAndHashCode.Exclude
  @JsonDeserialize(using = KindDeserializer.class)
  private Kind kind;

  @Key
  @EqualsAndHashCode.Exclude
  @JsonProperty("tags")
  private List<BaseTag> tags;

  @Key
  @EqualsAndHashCode.Exclude
  private String content;

  @Key
  @JsonProperty("sig")
  @EqualsAndHashCode.Exclude
  @JsonDeserialize(using = SignatureDeserializer.class)
  @Setter
  private Signature signature;

  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private byte[] _serializedEvent;

  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private Integer nip;

  @JsonIgnore
  @EqualsAndHashCode.Exclude
  private List<ElementAttribute> attributes;

  public GenericEventImpl() {
  }

  public GenericEventImpl(PublicKey publicKeySender) {
    this.pubKey = publicKeySender;
  }

  public GenericEventImpl(PublicKey publicKeySender, Kind kind) {
    this.pubKey = publicKeySender;
    this.kind = kind;
  }

  public GenericEventImpl(PublicKey publicKeySender, Kind kind, List<BaseTag> tags) {
    this.pubKey = publicKeySender;
    this.kind = kind;
    this.tags = tags;
  }

  public GenericEventImpl(PublicKey publicKeySender, Kind kind, List<BaseTag> tags, String content) {
    this.pubKey = publicKeySender;
    this.kind = kind;
    this.tags = tags;
    this.content = content;
  }

  @Override
  public void update() {
    // TODO: refactor procedural into OO, possibly utility class
    try {
      this.setCreatedAt(Instant.now().getEpochSecond());

      this._serializedEvent = this.serialize().getBytes(StandardCharsets.UTF_8);

      this.id = NostrUtil.bytesToHex(NostrUtil.sha256(_serializedEvent));
    } catch (NostrException | NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
  }

  private String serialize() throws NostrException {
    var mapper = IEncoder.MAPPER;
    var arrayNode = JsonNodeFactory.instance.arrayNode();

    try {
      arrayNode.add(0);
      arrayNode.add(this.getPubKey().toString());
      arrayNode.add(this.getCreatedAt());
      arrayNode.add(this.getKind().getValue());
      arrayNode.add(mapper.valueToTree(this.getTags()));
      arrayNode.add(this.getContent());

      return mapper.writeValueAsString(arrayNode);
    } catch (JsonProcessingException e) {
      throw new NostrException(e);
    }
  }

  protected final void updateTagsParents(List<? extends BaseTag> tagList) {
    if (tagList != null && !tagList.isEmpty()) {
      for (ITag t : tagList) {
        t.setParent(this);
      }
    }
  }

  @Override
  public String toBech32() {
    if (!isSigned()) {
      update();
    }

    try {
      return Bech32.toBech32(Bech32Prefix.NOTE, this.getId());
    } catch (NostrException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void setTags(List<BaseTag> tags) {

    this.tags = tags;

    for (ITag o : tags) {
      o.setParent(this);
    }
  }

  public void addTag(BaseTag tag) {

    if (!getTagsViaSingletion().contains(tag)) {
      tag.setParent(this);
      tags.add(tag);
    }
  }

  private List<BaseTag> getTagsViaSingletion() {
    if (tags == null)
      tags = new ArrayList<>();
    return tags;
  }


  @Transient
  public boolean isSigned() {
    return this.signature != null;
  }

  @Override
  public void addAttribute(ElementAttribute attribute) {
    this.attributes.add(attribute);
  }
}
