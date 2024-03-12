package nostr.event.impl;

import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.Kind;

import java.util.List;

public class EventDecorator implements GenericEventNick {
  private final GenericEventNick genericEvent;

  public EventDecorator(GenericEventNick genericEvent) {
    this.genericEvent = genericEvent;
  }

  @Override
  public String toBech32() {
    return genericEvent.toBech32();
  }

  @Override
  public String getId() {
    return genericEvent.getId();
  }

  @Override
  public List<ElementAttribute> getAttributes() {
    return genericEvent.getAttributes();
  }

  @Override
  public void addAttribute(ElementAttribute attribute) {
    genericEvent.addAttribute(attribute);
  }

  @Override
  public void setPubKey(PublicKey getPubKey) {
    genericEvent.setPubKey(getPubKey);
  }

  @Override
  public void setCreatedAt(Long createdAt) {
    genericEvent.setCreatedAt(createdAt);
  }

  @Override
  public Kind getKind() {
    return genericEvent.getKind();
  }

  @Override
  public void setKind(Kind kind) {
    genericEvent.setKind(kind);
  }

  @Override
  public void setTags(List<BaseTag> tags) {
    genericEvent.setTags(tags);
  }

  @Override
  public void addTag(BaseTag tag) {
    genericEvent.addTag(tag);
  }

  @Override
  public void setContent(String content) {
    genericEvent.setContent(content);
  }

  @Override
  public void setSignature(Signature signature) {
    genericEvent.setSignature(signature);
  }

  @Override
  public void setNip(Integer nip) {
    genericEvent.setNip(nip);
  }

  @Override
  public PublicKey getPubKey() {
    return genericEvent.getPubKey();
  }

  @Override
  public Long getCreatedAt() {
    return genericEvent.getCreatedAt();
  }

  @Override
  public List<BaseTag> getTags() {
    return genericEvent.getTags();
  }

  @Override
  public String getContent() {
    return genericEvent.getContent();
  }

  @Override
  public Signature getSignature() {
    return genericEvent.getSignature();
  }

  @Override
  public Integer getNip() {
    return genericEvent.getNip();
  }

  @Override
  public void update() {
    genericEvent.update();
  }

  @Override
  public byte[] get_serializedEvent() {
    return genericEvent.get_serializedEvent();
  }
}
