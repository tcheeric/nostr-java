package nostr.event.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.event.tag.EventTag;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingHistory {
    private Direction direction;
    private Amount amount;

    @Builder.Default
    private List<EventTag> eventTags = new ArrayList<>();

    public enum Direction {
        RECEIVED("in"),
        SENT("out");

        private final String value;

        Direction(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    public void addEventTag(@NonNull EventTag eventTag) {
        this.eventTags.add(eventTag);
    }

}
