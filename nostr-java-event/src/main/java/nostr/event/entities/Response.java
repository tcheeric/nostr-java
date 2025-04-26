package nostr.event.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.base.Relay;
import nostr.event.BaseMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

    private BaseMessage message;
    private Relay relay;
}
