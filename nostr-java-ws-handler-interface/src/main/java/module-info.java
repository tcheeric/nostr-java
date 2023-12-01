/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module nostr.ws.handler {
    requires nostr.util;
    requires static lombok;
    requires nostr.base;
    requires nostr.event;

    exports nostr.ws.handler.spi;
    exports nostr.ws.handler.command.spi;
    
    uses nostr.ws.handler.spi.IRequestHandler;
    uses nostr.ws.handler.spi.IResponseHandler;
    
    uses nostr.ws.handler.command.spi.ICommandHandler;
}
