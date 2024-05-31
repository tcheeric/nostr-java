
package nostr.event.list;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import nostr.base.FNostrList;
import nostr.event.impl.Filters;
import nostr.event.json.deserializer.CustomFiltersListDeserializer;

import java.util.List;

/**
 * @author squirrel
 */
@Builder
@JsonDeserialize(using = CustomFiltersListDeserializer.class)
public class FiltersList extends FNostrList<Filters> {

  public FiltersList() {
      super();
  }

  public FiltersList(Filters... filters) {
      this(List.of(filters));
  }

  public FiltersList(@NonNull List<Filters> list) {
      super();
      addAll(list);
  }
}
