package nostr.command.impl;

import nostr.command.AbstractCommand;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultResponseContext;

public class EoseCommand extends AbstractCommand {

    public EoseCommand() {
        super("eose");
    }

    @Override
    public void execute(CommandContext context) {
        if (context instanceof DefaultResponseContext defaultResponseContext) {
            var subId = defaultResponseContext.getSubscriptionId();
            var relay = defaultResponseContext.getRelay();
            getHandler().onEose(subId, relay);
        }
    }
}
