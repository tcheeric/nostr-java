package nostr.command.impl;

import nostr.command.AbstractCommand;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultResponseContext;

public class NoticeCommand extends AbstractCommand {

    public NoticeCommand() {
        super("notice");
    }

    @Override
    public void execute(CommandContext context) {
        if (context instanceof DefaultResponseContext defaultResponseContext) {
            var message = defaultResponseContext.getMessage();

            getHandler().onNotice(message);
        }
    }
}
