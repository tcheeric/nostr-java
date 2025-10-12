package nostr.event.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.EqualsAndHashCode;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.VoteTag;

import java.util.function.Function;
import java.util.function.Predicate;

@EqualsAndHashCode(callSuper = true)
public class VoteTagFilter<T extends VoteTag> extends AbstractFilterable<T> {
  public static final String FILTER_KEY = "#v";

  public VoteTagFilter(T voteTag) {
    super(voteTag, FILTER_KEY);
  }

  @Override
  public Predicate<GenericEvent> getPredicate() {
    return (genericEvent) ->
        Filterable.getTypeSpecificTags(VoteTag.class, genericEvent).stream()
            .anyMatch(voteTag -> voteTag.getVote().equals(getFilterableValue()));
  }

  @Override
  public Integer getFilterableValue() {
    return getVoteTag().getVote();
  }

  private T getVoteTag() {
    return super.getFilterable();
  }

  public static Function<JsonNode, Filterable> fxn =
      node -> new VoteTagFilter<>(new VoteTag(node.asInt()));
}
