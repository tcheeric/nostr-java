package nostr.controller.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.CustomHandler;
import nostr.base.annotation.DefaultHandler;
import nostr.command.CommandHandler;
import nostr.context.Context;
import nostr.context.impl.DefaultCommandContext;
import nostr.controller.ApplicationController;
import nostr.util.thread.ThreadUtil;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;

@Getter
@Log
public class ApplicationControllerImpl implements ApplicationController {

    private final String command;

    public ApplicationControllerImpl(@NonNull String command) {
        this.command = command;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void handleRequest(Context requestContext) {
        requestContext.validate();
        if (requestContext instanceof DefaultCommandContext defaultCommandContext) {
            try {
                ThreadUtil.builder().blocking(true).task(this).build().run(defaultCommandContext);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Void execute(@NonNull Context context) {
        if (context instanceof DefaultCommandContext commandContext) {
            executeCommand(commandContext);
        }
        return null;
    }

    private void executeCommand(@NonNull DefaultCommandContext defaultCommandContext) {

        var commandHandler = getCommandHandler(this.command);

        log.fine("Executing command: " + this.command);
        commandHandler.handle(defaultCommandContext);
    }

    private static CommandHandler getCommandHandler(@NonNull String command) {
        ServiceLoader<CommandHandler> loader = ServiceLoader.load(CommandHandler.class);

        Optional<CommandHandler> customHandler = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(ch -> ch.getClass().isAnnotationPresent(CustomHandler.class) && ch.getClass().getAnnotation(CustomHandler.class).command().equals(Command.valueOf(command.toUpperCase())))
                .findFirst();

        if (customHandler.isPresent()) {
            return customHandler.get();
        }

        Optional<CommandHandler> defaultHandler = loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(ch -> ch.getClass().isAnnotationPresent(DefaultHandler.class) && ch.getClass().getAnnotation(DefaultHandler.class).command().equals(Command.valueOf(command.toUpperCase())))
                .findFirst();

        if (defaultHandler.isPresent()) {
            return defaultHandler.get();
        }

        throw new AssertionError("Could not load the default handler");
    }
}
