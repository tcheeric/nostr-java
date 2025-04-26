package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.net.URL;

import static nostr.base.IEvent.MAPPER_AFTERBURNER;

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
