package nostr.api.nip01;

import java.util.List;
import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.event.filter.Filters;
import nostr.event.message.CloseMessage;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.NoticeMessage;
import nostr.event.message.ReqMessage;

/**
 * Creates protocol messages referenced by {@link nostr.api.NIP01}.
 */
public final class NIP01MessageFactory {

  private NIP01MessageFactory() {}

  public static EventMessage eventMessage(@NonNull GenericEvent event, String subscriptionId) {
    return subscriptionId != null ? new EventMessage(event, subscriptionId) : new EventMessage(event);
  }

  public static ReqMessage reqMessage(@NonNull String subscriptionId, @NonNull List<Filters> filters) {
    return new ReqMessage(subscriptionId, filters);
  }

  public static CloseMessage closeMessage(@NonNull String subscriptionId) {
    return new CloseMessage(subscriptionId);
  }

  public static EoseMessage eoseMessage(@NonNull String subscriptionId) {
    return new EoseMessage(subscriptionId);
  }

  public static NoticeMessage noticeMessage(@NonNull String message) {
    return new NoticeMessage(message);
  }
}
