/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module nostr.types {
    requires static lombok;
    requires java.logging;
    
    exports nostr.types;
    exports nostr.types.values;
    exports nostr.types.values.impl;
    exports nostr.types.values.marshaller;
}
