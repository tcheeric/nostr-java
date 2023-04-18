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

    public static boolean checkSupport(@NonNull Relay relay, IEvent event) {

        if (event == null) {
            return true;
        }

        var e = event.getClass().getDeclaredAnnotation(Event.class);
        var nip = e == null ? event.getNip() : e.nip();
        return relay.getSupportedNips().contains(nip);
    }

    public static boolean checkSupport(@NonNull Relay relay, @NonNull ITag tag) {

        var t = tag.getClass().getDeclaredAnnotation(Tag.class);
        int nip = t == null ? 1 : t.nip();
        return relay.getSupportedNips().contains(nip);
    }

    public static boolean checkSupport(@NonNull Relay relay, @NonNull GenericTagQuery gtq) {

        return relay.getSupportedNips().contains(12);
    }

    public int getNip(IEvent event) {
        return event.getNip() == null ? 1 : event.getNip();
    }
}
