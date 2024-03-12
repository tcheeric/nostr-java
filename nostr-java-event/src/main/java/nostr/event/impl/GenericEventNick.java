package nostr.event.impl;

import nostr.base.IGenericElement;
import nostr.base.ISignable;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseEventNick;
import nostr.event.BaseTag;
import nostr.event.Kind;

import java.util.List;

public interface GenericEventNick extends BaseEventNick, ISignable, IGenericElement {
  PublicKey getPubKey();

  void setPubKey(PublicKey getPubKey);

  Long getCreatedAt();

  void setCreatedAt(Long createdAt);

  Kind getKind();

  void setKind(Kind kind);

  List<BaseTag> getTags();

  void setTags(List<BaseTag> tags);

  void addTag(BaseTag tag);

  String getContent();

  void setContent(String content);

  Signature getSignature();

  void setSignature(Signature signature);

  Integer getNip();

  void setNip(Integer nip);

  byte[] get_serializedEvent();

  void update();
}
