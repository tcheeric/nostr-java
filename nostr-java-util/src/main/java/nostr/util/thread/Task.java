package nostr.util.thread;

import lombok.NonNull;
import nostr.context.Context;

public interface Task<T> {

    T execute(@NonNull Context context);
}
