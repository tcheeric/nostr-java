package nostr.base;

import java.lang.reflect.Field;

import lombok.NonNull;
import nostr.base.annotation.Event;
import nostr.base.annotation.Key;
import nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
public class NipUtil {

    public static boolean checkSupport(@NonNull Relay relay, @NonNull Field field) {

        var k = field.getDeclaredAnnotation(Key.class);
        int nip = k == null ? 1 : k.nip();
        return relay.getSupportedNips().contains(nip);
    }

    public static boolean checkSupport(@NonNull Relay relay, IElement element) {
        if (element == null) {
            return true;
        }

        var nip = 1;
        if (element instanceof IEvent event) {
            var e = element.getClass().getDeclaredAnnotation(Event.class);
            nip = (e == null) ? event.getNip() : e.nip();
        } else if (element instanceof ITag) {
            var t = element.getClass().getDeclaredAnnotation(Tag.class);
            nip = (t == null) ? nip : t.nip();
        }

        return relay.getSupportedNips().contains(nip);
    }

    public static boolean checkSupport(@NonNull Relay relay, @NonNull GenericTagQuery gtq) {

        return relay.getSupportedNips().contains(12);
    }
}
