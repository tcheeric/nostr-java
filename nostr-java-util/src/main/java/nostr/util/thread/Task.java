package nostr.util.thread;

import lombok.NonNull;
import nostr.context.Context;

import java.io.IOException;

public interface Task<T> {

    T execute(@NonNull Context context) throws IOException;
}
