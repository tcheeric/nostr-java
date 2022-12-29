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
        int nip = e == null ? 1 : e.nip();
        return relay.getSupportedNips().contains(nip);
    }

    public static boolean checkSupport(@NonNull Relay relay, @NonNull ITag tag) {

        var t = tag.getClass().getDeclaredAnnotation(Tag.class);
        int nip = t == null ? 1 : t.nip();
        return relay.getSupportedNips().contains(nip);
    }
}
