
package nostr.event.impl;

import nostr.base.Profile;
import nostr.event.Kind;
import nostr.base.PublicKey;
import nostr.event.list.TagList;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.annotation.Event;
import nostr.types.values.IValue;
import nostr.types.values.impl.ExpressionValue;
import nostr.types.values.impl.ObjectValue;
import nostr.types.values.impl.StringValue;
import nostr.util.NostrException;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Event(name = "Metadata")
public final class MetadataEvent extends GenericEvent {

    private static final String NAME_PATTERN = "\\w[\\w\\-]+\\w";

    private Profile profile;

    public MetadataEvent(PublicKey pubKey, TagList tagList, Profile profile) throws NostrException, NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
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
    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        setContent();
        
        super.update();
    }

    private void setContent() {        
        IValue nameValue = new StringValue(this.getProfile().getName());
        ExpressionValue nameExpr = new ExpressionValue("name", nameValue);

        IValue aboutValue = new StringValue(this.getProfile().getAbout());
        ExpressionValue aboutExpr = new ExpressionValue("about", aboutValue);

        IValue picValue = new StringValue(this.getProfile().getPicture().toString());
        ExpressionValue picExpr = new ExpressionValue("picture", picValue);

        List<ExpressionValue> value = new ArrayList<>();
        value.add(nameExpr);
        value.add(aboutExpr);
        value.add(picExpr);

        ObjectValue content = new ObjectValue(value);
        setContent(content.toString(true));
    }

}
