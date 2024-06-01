package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;
import nostr.base.FNostrList;
import nostr.event.impl.Filters;
import nostr.event.json.deserializer.CustomFiltersListDeserializer;

@JsonDeserialize(using = CustomFiltersListDeserializer.class)
public class FiltersList extends FNostrList<Filters> {
  // TODO: revisit below constructor, they shouldn't be needed since extending FNostrList, but compiler
  //    complains without them
//  public FiltersList() {
//    super();
//  }

  // TODO: revisit below constructor, they shouldn't be needed since extending FNostrList, but compiler
  //    complains without them
//  public FiltersList(@NonNull Filters filters) {
//    super();
//    add(filters);
//  }
}
