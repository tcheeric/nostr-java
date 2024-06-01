//package nostr.event.list;
//
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import lombok.NonNull;
//import nostr.event.BaseEvent.ProxyEvent;
//import nostr.event.json.deserializer.CustomEventListDeserializer;
//
//import java.util.List;
//
//@JsonDeserialize(using = CustomEventListDeserializer.class)
//public class ProxyEventList extends BaseList<ProxyEvent> {
//  public ProxyEventList() {
//    super();
//  }
//
//  public ProxyEventList(@NonNull ProxyEvent proxyEvent) {
//    this(List.of(proxyEvent));
//  }
//
//  public ProxyEventList(@NonNull List<ProxyEvent> list) {
//    this.addAll(list);
//  }
//}
