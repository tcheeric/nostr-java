package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;

import java.util.function.Function;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class ReferencedPublicKeyFilter<T extends PubKeyTag> extends AbstractFilterable<T> {
    public final static String FILTER_KEY = "#p";

    public ReferencedPublicKeyFilter(T referencedPubKeyTag) {
        super(referencedPubKeyTag, FILTER_KEY);
    }

    @Override
    public Predicate<GenericEvent> getPredicate() {
        return (genericEvent) ->
                Filterable.getTypeSpecificTags(PubKeyTag.class, genericEvent).stream()
                        .anyMatch(pubKeyTag ->
                                pubKeyTag.getPublicKey().toHexString().equals(getFilterableValue()));
    }

    @Override
    public String getFilterableValue() {
        return getReferencedPublicKey().getPublicKey().toHexString();
    }

    private T getReferencedPublicKey() {
        return super.getFilterable();
    }

    public static Function<JsonNode, Filterable> fxn = node -> new ReferencedPublicKeyFilter<>(new PubKeyTag(new PublicKey(node.asText())));
}
