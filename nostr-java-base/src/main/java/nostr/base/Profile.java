package nostr.base;

import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author eric
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public abstract class Profile {

    private final String name;

    @ToString.Exclude
    private String about;

    @ToString.Exclude
    private URL picture;

}
