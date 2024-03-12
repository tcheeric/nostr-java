package nostr.event.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nostr.base.annotation.Event;
import nostr.event.AbstractEventContent;
import nostr.event.BaseTag;
import nostr.event.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author eric
 */
@EqualsAndHashCode(callSuper = false)
@Event(name = "Create or update a stall", nip = 15)
public class CreateOrUpdateStallEventNick extends EventDecorator {
  private final Stall stall;

  public CreateOrUpdateStallEventNick(GenericEventNick genericEvent, List<BaseTag> tags, Stall stall) {
    super(genericEvent);
    setTags(tags);
    setKind(Kind.KIND_SET_STALL);
    setContent(stall.toString());
    this.stall = stall;
  }

  @Getter
  @Setter
  @EqualsAndHashCode(callSuper = false)
  public static class Stall extends AbstractEventContent<CreateOrUpdateStallEventNick> {

    @JsonProperty
    private final String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String currency;

    @JsonProperty
    private Shipping shipping;

    public Stall() {
      this.id = UUID.randomUUID().toString();
    }

    @Data
    public static class Shipping {

      @JsonProperty
      private final String id;

      @JsonProperty
      private String name;

      @JsonProperty
      private Float cost;

      @JsonProperty
      private List<String> countries;

      public Shipping() {
        this.countries = new ArrayList<>();
        this.id = UUID.randomUUID().toString();
      }
    }
  }
}
