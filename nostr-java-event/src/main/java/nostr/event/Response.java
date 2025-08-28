package nostr.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.base.Relay;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

  private BaseMessage message;
  private Relay relay;
}
