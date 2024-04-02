package nostr.command.impl;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.annotation.DefaultHandler;
import nostr.command.Command;
import nostr.ws.handler.command.spi.ICommandHandler;

import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.logging.Level;

@Log
public class CommandFactory {

    static CommandFactory instance;

    private CommandFactory() {
    }

    public static CommandFactory getInstance() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }

    public Command createCommand(@NonNull String commandName) {
        Command command;

        switch (commandName.toLowerCase()) {
            case "eose" -> command = new EoseCommand();
            case "event" -> command = new EventCommand();
            case "notice" -> command = new NoticeCommand();
            case "ok" -> command = new OkCommand();
            case "auth" -> command = new AuthCommand();
            default -> throw new RuntimeException("Invalid command.");
        }

        //loadCommandHandler(command);
        return command;
    }

    private static void loadCommandHandler(Command command) {
        ICommandHandler commandHandler;
        try {
            commandHandler = ServiceLoader
                    .load(ICommandHandler.class)
                    .stream()
                    .map(p -> p.get())
                    .filter(ch -> !ch.getClass().isAnnotationPresent(DefaultHandler.class))
                    .findFirst()
                    .get();
        } catch (NoSuchElementException ex) {
            log.log(Level.WARNING, "No custom command handler provided. Using default command handler instead...");
            try {
                commandHandler = ServiceLoader
                        .load(ICommandHandler.class)
                        .stream()
                        .map(p -> p.get())
                        .filter(ch -> ch.getClass().isAnnotationPresent(DefaultHandler.class))
                        .findFirst()
                        .get();
            } catch (NoSuchElementException e) {
                throw new AssertionError("Could not load the default handler", e);
            }
        }

        command.setCommandHandler(commandHandler);
    }
}
