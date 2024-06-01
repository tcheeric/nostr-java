//package nostr.event.list;
//
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import lombok.NonNull;
//import nostr.base.FNostrList;
//import nostr.event.json.deserializer.CustomKindListDeserializer;
//
//import java.util.List;
//
//@JsonDeserialize(using = CustomKindListDeserializer.class)
//public class KindList extends FNostrList<Integer> {
//    public KindList() {
//        super();
//    }
//
//    public KindList(Integer... kinds) {
//        this(List.of(kinds));
//    }
//
//    public KindList(@NonNull List<Integer> list) {
//        super.addAll(list);
//    }
//}
