
package nostr.event.list;

import lombok.Builder;
import lombok.NonNull;
import nostr.event.impl.GenericEventNick;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squirrel
 */
@Builder
// TODO - public class EventList extends BaseList<? extends GenericEvent>
public class EventListNick extends BaseList<GenericEventNick> {

  public EventListNick() {
    this(new ArrayList<>());
  }

  private EventListNick(@NonNull List<GenericEventNick> list) {
    super(list);
  }
}
