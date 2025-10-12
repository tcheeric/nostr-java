package nostr.event.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author eric
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChannelProfile extends Profile {

  public ChannelProfile(String name, String about, URL picture) {
    super(name, about, picture);
  }

  public ChannelProfile(String name, String about, String url)
      throws MalformedURLException, URISyntaxException {
    this(name, about, new URI(url).toURL());
  }
}
