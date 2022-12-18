
package com.tcheeric.nostr.base;

import java.net.URL;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
@EqualsAndHashCode
public final class Profile {
    
    private final String name;
        
    @ToString.Exclude
    private final PublicKey publicKey;
    
    private String about;
    
    @ToString.Exclude
    private URL picture;    
    
    private String email;    
}
