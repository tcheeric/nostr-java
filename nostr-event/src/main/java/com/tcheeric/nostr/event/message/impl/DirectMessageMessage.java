package com.tcheeric.nostr.event.message.impl;

import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.impl.GenericEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class DirectMessageMessage {

    private final GenericEvent parent;
    private final PublicKey recipient;
    private final String content;
    
}
