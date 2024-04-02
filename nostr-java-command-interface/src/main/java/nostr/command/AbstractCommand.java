package nostr.command;

import lombok.Data;
import lombok.NonNull;

@Data
public abstract class AbstractCommand implements Command {

    private String name;

    public AbstractCommand(@NonNull String name) {
        this.name = name;
    }

}
