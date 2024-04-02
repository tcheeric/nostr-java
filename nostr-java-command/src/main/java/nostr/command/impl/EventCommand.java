package nostr.command.impl;

import nostr.command.AbstractCommand;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultResponseContext;

public class EventCommand extends AbstractCommand {

        public EventCommand() {
            super("event");
        }

        @Override
        public void execute(CommandContext context) {
            if (context instanceof DefaultResponseContext defaultResponseContext) {
                var jsonEvent = defaultResponseContext.getJsonEvent();
                var subId = defaultResponseContext.getSubscriptionId();
                var relay = defaultResponseContext.getRelay();

                getHandler().onEvent(jsonEvent, subId, relay);
            }
        }
}
