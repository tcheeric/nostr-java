package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CashuMint {
    @EqualsAndHashCode.Include
    private final String url;
    private List<String> units;

    @Override
    @JsonValue
    public String toString() {
        return url;
    }
}
