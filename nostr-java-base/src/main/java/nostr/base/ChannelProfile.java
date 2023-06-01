package nostr.base;

import java.net.URL;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author eric
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChannelProfile extends Profile {    

    public ChannelProfile(String name, String about, URL picture) {
        super(name, about, picture);
    }
}
