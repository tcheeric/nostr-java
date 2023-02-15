package nostr.event.impl;

import nostr.base.Profile;
import nostr.event.Kind;
import nostr.base.PublicKey;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.Event;
import nostr.event.list.TagList;
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

    public InternetIdentifierMetadataEvent(PublicKey pubKey, TagList tags, @NonNull Profile profile) {
        super(pubKey, Kind.SET_METADATA, tags);
        this.name = profile.getName();
        this.nip05 = profile.getNip05();
    }

    @Override
    public void update() throws NostrException {
        
        try {
            // NIP-05 validator
            Nip05Validator.builder().nip05(nip05).publicKey(getPubKey()).build().validate();
            
            setContent();
            
            super.update();
        } catch (NoSuchAlgorithmException | IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
            log.log(Level.SEVERE, null, ex);
            throw new NostrException(ex);
        }

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
