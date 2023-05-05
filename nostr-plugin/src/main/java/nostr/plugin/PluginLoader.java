/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nostr.plugin;

import java.util.Optional;
import java.util.ServiceLoader;
import lombok.extern.java.Log;
import nostr.base.annotation.DefaultHandler;
import nostr.ws.handler.command.spi.ICommandHandler;
import nostr.ws.handler.spi.IRequestHandler;
import nostr.ws.handler.spi.IResponseHandler;

/**
 *
 * @author squirrel
 */
@Log
public class PluginLoader {

    public static Optional<ICommandHandler> loadCommandHandler() {

        return ServiceLoader
                .load(ICommandHandler.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(ch -> !ch.getClass().isAnnotationPresent(DefaultHandler.class))
                .findFirst();
    }

    public static Optional<IRequestHandler> loadRequestHandler() {
        return ServiceLoader
                .load(IRequestHandler.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .findFirst();
    }

    public static Optional<IResponseHandler> loadResponseHandler() {
        return ServiceLoader
                .load(IResponseHandler.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .findFirst();
    }

}
