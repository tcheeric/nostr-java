package nostr.controller.command;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.Command;
import nostr.base.annotation.CustomHandler;
import nostr.base.annotation.DefaultHandler;
import nostr.context.Context;
import nostr.context.impl.DefaultCommandContext;
import nostr.controller.CommandController;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.ws.handler.command.CommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@Log
public class CommandControllerImpl implements CommandController {

    private ExecutorService executorService;

    @Getter
    private final String command;

    @NonNull
    @Getter
    private List<BaseMessage> responses;

    public CommandControllerImpl(@NonNull String command) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.command = command;
        this.responses = new ArrayList<>();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void handleRequest(Context requestContext) {
        requestContext.validate();
        try {
            if (requestContext instanceof DefaultCommandContext defaultCommandContext) {
                executorService.submit(() -> {
                    executeCommand(defaultCommandContext);
                });
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return;
    }

    private void executeCommand(@NonNull DefaultCommandContext defaultCommandContext) {

        var commandHandler = getCommandHandler(this.command);

        commandHandler.handle(defaultCommandContext);

        var msg = defaultCommandContext.getMessage();
        BaseMessageDecoder decoder = new BaseMessageDecoder(msg);
        var message = decoder.decode();
        this.responses.add(message);
    }

    private static CommandHandler getCommandHandler(@NonNull String command) {

        try {
            return ServiceLoader
                    .load(CommandHandler.class)
                    .stream()
                    .map(p -> p.get())
                    .filter(ch -> ch.getClass().isAnnotationPresent(CustomHandler.class) && ch.getClass().getAnnotation(CustomHandler.class).command().equals(Command.valueOf(command.toUpperCase())))
                    .findFirst()
                    .get();
        } catch (NoSuchElementException ex) {
            log.log(Level.INFO, "No custom command handler provided. Using default command handler instead...");
            try {
                return ServiceLoader
                        .load(CommandHandler.class)
                        .stream()
                        .map(p -> p.get())
                        .filter(ch -> ch.getClass().isAnnotationPresent(DefaultHandler.class) && ch.getClass().getAnnotation(DefaultHandler.class).command().equals(Command.valueOf(command.toUpperCase())))
                        .findFirst()
                        .get();
            } catch (NoSuchElementException e) {
                throw new AssertionError("Could not load the default handler", e);
            }
        }
    }
}
