package com.tcheeric.nostr.base;

import com.tcheeric.nostr.base.annotation.JsonString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class BaseKey implements IKey {
    
    @NonNull
    @EqualsAndHashCode.Exclude
    protected final KeyType type;
    
    @NonNull
    @JsonString
    protected final byte[] rawData;
    
    @Override
    public String toString() {
        return NostrUtil.bytesToHex(rawData);
    }
    
}
