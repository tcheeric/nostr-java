/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event;

import com.tcheeric.nostr.base.IEvent;
import com.tcheeric.nostr.base.ITag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.java.Log;
import com.tcheeric.nostr.base.annotation.Tag;

/**
 *
 * @author squirrel
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
@Log
public abstract class BaseTag implements ITag {

    private IEvent parent;

    @Override
    public void setParent(IEvent event) {
        this.parent = event;
    }

    public String getCode() {
        var tag = this.getClass().getAnnotation(Tag.class);
        return tag.code();
    }
}
