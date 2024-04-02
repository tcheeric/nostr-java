package nostr.command.impl;


import nostr.command.AbstractCommand;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultResponseContext;

public class OkCommand extends AbstractCommand {

    public OkCommand() {
        super("ok");
    }

    @Override
    public void execute(CommandContext context) {
        if (context instanceof DefaultResponseContext defaultResponseContext) {
            var eventId = defaultResponseContext.getEventId();
            var subId = defaultResponseContext.getSubscriptionId();
            var relay = defaultResponseContext.getRelay();
            var result = defaultResponseContext.isResult();
            var message = defaultResponseContext.getMessage();

            getHandler().onOk(eventId, message, null, result, relay);
        }
    }
}
