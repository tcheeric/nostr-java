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
import nostr.event.Response;
import nostr.util.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Getter
@Log
public class ApplicationControllerImpl implements ApplicationController {

    private final String command;

    @NonNull
    private final List<Response> responses;

    public ApplicationControllerImpl(@NonNull String command) {
        this.command = command;
        this.responses = new ArrayList<>();
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

        log.info("Executing command: " + this.command);
        commandHandler.handle(defaultCommandContext);

        log.log(Level.INFO, "Command executed successfully. Adding response to the controller...");
        var message = defaultCommandContext.getMessage();
        var response = Response.builder().message(message).relay(defaultCommandContext.getRelay()).build();

        this.responses.add(response);
        log.log(Level.INFO, "Added response...{0}", response);
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
