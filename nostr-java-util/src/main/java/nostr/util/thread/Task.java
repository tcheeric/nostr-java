package nostr.util.thread;

import lombok.NonNull;
import nostr.context.Context;

public interface Task<T> {

    public T execute(@NonNull Context context);
}
