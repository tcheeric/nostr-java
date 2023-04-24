
package nostr.event.impl;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import nostr.base.IMarshaller;
import nostr.base.Profile;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.Kind;
import nostr.event.list.TagList;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Metadata")
@Log
public final class MetadataEvent extends GenericEvent {

    private static final String NAME_PATTERN = "\\w[\\w\\-]+\\w";

    @JsonIgnore
    private Profile profile;

    public MetadataEvent(PublicKey pubKey, TagList tagList, Profile profile) throws NostrException {
        super(pubKey, Kind.SET_METADATA, tagList);
        this.profile = profile;

        this.validate();
    }

    private void validate() throws NostrException {
        var valid = this.profile.getName().matches(NAME_PATTERN);
        if (!valid) {
            throw new NostrException("Invalid profile name: " + this.profile);
        }
    }

    @Override
    public void update() throws NostrException {
        setContent();
        
        try {
            super.update();
        } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }
    }

    private void setContent() {
	    var mapper = IMarshaller.MAPPER;
    	try {
	    	ObjectNode objNode = JsonNodeFactory.instance.objectNode();
	    	objNode.set("name", mapper.valueToTree(this.getProfile().getName()));
	    	objNode.set("about", mapper.valueToTree(this.getProfile().getAbout()));
	    	objNode.set("picture", mapper.valueToTree(this.getProfile().getPicture().toString()));
	    	
	    	setContent(mapper.writeValueAsString(objNode));
		} catch (Exception e) {
            log.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
		} 
    }

}
