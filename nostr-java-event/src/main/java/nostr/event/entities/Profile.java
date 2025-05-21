package nostr.event.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.net.URL;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Profile {

    private String name;

    @ToString.Exclude
    private String about;

    @ToString.Exclude
    private URL picture;
}
