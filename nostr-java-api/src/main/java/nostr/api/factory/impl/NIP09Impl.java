/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.api.factory.EventFactory;
import nostr.event.BaseTag;
import nostr.event.impl.DeletionEvent;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP09Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DeletionEventFactory extends EventFactory<DeletionEvent> {
    	
    	public DeletionEventFactory(Identity sender) {
    		super(sender);
    	}

        public DeletionEventFactory(List<BaseTag> tags) {
            super(tags, null);
        }

        public DeletionEventFactory(Identity sender, List<BaseTag> tags) {
            super(sender, tags, null);
        }


        @Override
        public DeletionEvent create() {
            return new DeletionEvent(getSender(), getTags());
        }

    }

    public static class Kinds {

        public static final Integer KIND_DELETION = 5;
    }
    
}
