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
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.event.util.Nip05Validator;
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
@Log
@Event(name = "Internet Identifier Metadata Event", nip = 5)
public final class InternetIdentifierMetadataEvent extends GenericEvent {

    private final String name;
    private final String nip05;

    public InternetIdentifierMetadataEvent(PublicKey pubKey, TagList tags, @NonNull Profile profile) throws NostrException, NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        super(pubKey, Kind.SET_METADATA, tags);
        this.name = profile.getName();
        this.nip05 = profile.getEmail();
    }

    @Override
    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        
        // NIP-05 validator
        Nip05Validator.builder().nip05(nip05).publicKey(getPubKey()).build().validate();

        setContent();

        super.update();

    }

    private void setContent() {
        IValue nameValue = new StringValue(this.name);
        ExpressionValue nameExpr = new ExpressionValue("name", nameValue);

        IValue nip05Value = new StringValue(this.nip05);
        ExpressionValue nip05Expr = new ExpressionValue("nip05", nip05Value);

        List<ExpressionValue> expressions = new ArrayList<>();
        expressions.add(nameExpr);
        expressions.add(nip05Expr);

        ObjectValue content = new ObjectValue(expressions);

        setContent(content.toString());
    }
}
