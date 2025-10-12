package nostr.event.message;

import nostr.event.BaseMessage;

/**
 * @author eric
 */
public abstract class BaseAuthMessage extends BaseMessage {

  public BaseAuthMessage(String command) {
    super(command);
  }

  @Override
  public String getNip() {
    return "42";
  }
}
