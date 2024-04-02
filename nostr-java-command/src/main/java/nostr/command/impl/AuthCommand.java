package nostr.command.impl;

import nostr.command.AbstractCommand;
import nostr.context.CommandContext;
import nostr.context.impl.DefaultResponseContext;

public class AuthCommand extends AbstractCommand {

    public AuthCommand() {
        super("auth");
    }

    @Override
    public void execute(CommandContext context) {
        if (context instanceof DefaultResponseContext defaultResponseContext) {
            var relay = defaultResponseContext.getRelay();
            var challenge = defaultResponseContext.getChallenge();

            getHandler().onAuth(challenge, relay);
        }
    }
}
