package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import nostr.base.IEncoder;
import nostr.base.UserProfile;
import nostr.base.annotation.Event;

/**
 * @author squirrel
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "Metadata")
public final class MetadataEventNick extends EventDecorator implements UpdatableEvent, ValidatableEvent {

  private static final String NAME_PATTERN = "\\w[\\w\\-]+\\w";

  @JsonIgnore
  private final UserProfile profile;
  private final GenericEventNick genericEvent;

  public MetadataEventNick(GenericEventNick genericEvent, UserProfile profile) {
    super(genericEvent);
    this.genericEvent = genericEvent;
    this.profile = profile;
  }

  @Override
  public void validate() {
    // TODO: refactor procedural into OO
    boolean valid = true;

    var strNameArr = this.profile.getNip05().split("@");
    if (strNameArr.length == 2) {
      var localPart = strNameArr[0];
      valid = localPart.matches(NAME_PATTERN);
    }

    if (!valid) {
      throw new AssertionError("Invalid profile name: " + this.profile, null);
    }
  }

  @Override
  public void update() {
    setContent();
    genericEvent.update();
  }

  private void setContent() {
    var mapper = IEncoder.MAPPER;
    try {
      ObjectNode objNode = JsonNodeFactory.instance.objectNode();
      objNode.set("name", mapper.valueToTree(profile.getName()));
      objNode.set("about", mapper.valueToTree(profile.getAbout()));
      objNode.set("picture", mapper.valueToTree(profile.getPicture().toString()));

      setContent(mapper.writeValueAsString(objNode));
    } catch (JsonProcessingException | IllegalArgumentException e) {
      throw new RuntimeException(e);
    }
  }

}
