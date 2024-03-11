package nostr.event.impl;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.IEncoder;
import nostr.base.PublicKey;
import nostr.base.UserProfile;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP01Event;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Metadata")
public final class MetadataEvent extends NIP01Event {

    private static final String NAME_PATTERN = "\\w[\\w\\-]+\\w";

    @JsonIgnore
    private UserProfile profile;

    public MetadataEvent(PublicKey pubKey, UserProfile profile) {
        super(pubKey, Kind.SET_METADATA, new ArrayList<BaseTag>());
        this.profile = profile;
    }

    @Override
    protected void validate() {
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

        super.update();
    }

    private void setContent() {
        var mapper = IEncoder.MAPPER;
        try {
            ObjectNode objNode = JsonNodeFactory.instance.objectNode();
            objNode.set("name", mapper.valueToTree(this.getProfile().getName()));
            objNode.set("about", mapper.valueToTree(this.getProfile().getAbout()));
            objNode.set("picture", mapper.valueToTree(this.getProfile().getPicture().toString()));

            setContent(mapper.writeValueAsString(objNode));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

}
