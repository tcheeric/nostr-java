package nostr.base;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Mint {
    private final String url;
    private List<String> units;

    @Override
    @JsonValue
    public String toString() {
        return url;
    }
}
