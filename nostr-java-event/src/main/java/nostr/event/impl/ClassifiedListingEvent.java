package nostr.event.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.base.annotation.Event;
import nostr.event.BaseTag;
import nostr.base.Kind;
import nostr.event.NIP99Event;
import nostr.event.json.deserializer.ClassifiedListingEventDeserializer;
import nostr.event.tag.GenericTag;
import nostr.event.tag.PriceTag;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Event(name = "ClassifiedListingEvent", nip = 99)
@JsonDeserialize(using = ClassifiedListingEventDeserializer.class)
@NoArgsConstructor
public class ClassifiedListingEvent extends NIP99Event {

    public ClassifiedListingEvent(PublicKey pubKey, Kind kind, List<BaseTag> tags, String content) {
        super(pubKey, kind, tags, content);
    }

    @Getter
    public enum Status {
        ACTIVE("active"),
        SOLD("sold");

        private final String value;

        Status(@NonNull String value) {
            this.value = value;
        }

    }

    public Instant getPublishedAt() {
        GenericTag publishedAtTag = getTag("published_at");
        return Instant.ofEpochSecond(Long.parseLong(publishedAtTag.getAttributes().get(0).getValue().toString()));
    }

    public String getLocation() {
        GenericTag locationTag = getTag("location");
        return locationTag.getAttributes().get(0).getValue().toString();
    }

    public String getTitle() {
        GenericTag titleTag = getTag("title");
        return titleTag.getAttributes().get(0).getValue().toString();
    }

    public String getSummary() {
        GenericTag summaryTag = getTag("summary");
        return summaryTag.getAttributes().get(0).getValue().toString();
    }

    public String getImage() {
        GenericTag imageTag = getTag("image");
        return imageTag.getAttributes().get(0).getValue().toString();
    }

    public Status getStatus() {
        GenericTag statusTag = getTag("status");
        String status = statusTag.getAttributes().get(0).getValue().toString();
        return Status.valueOf(status);
    }

    public String getPrice() {
        PriceTag priceTag = (PriceTag) getTags().stream()
                .filter(tag -> tag instanceof PriceTag)
                .findFirst()
                .orElseThrow();

        return priceTag.getNumber().toString() + " " + priceTag.getCurrency() +
               (priceTag.getFrequency() != null ? " " + priceTag.getFrequency() : "");
    }

    @Override
    protected void validateTags() {
        super.validateTags();

        // Validate published_at
        GenericTag publishedAtTag = getTag("published_at");
        if (publishedAtTag == null) {
            throw new AssertionError("Missing `published_at` tag for the publication date/time.");
        }
        try {
            Long.parseLong(publishedAtTag.getAttributes().get(0).getValue().toString());
        } catch (NumberFormatException e) {
            throw new AssertionError("Invalid `published_at` tag value: must be a numeric timestamp.");
        }

        // Validate location
        if (getTag("location") == null) {
            throw new AssertionError("Missing `location` tag for the listing location.");
        }

        // Validate title
        if (getTag("title") == null) {
            throw new AssertionError("Missing `title` tag for the listing title.");
        }

        // Validate summary
        if (getTag("summary") == null) {
            throw new AssertionError("Missing `summary` tag for the listing summary.");
        }

        // Validate image
        if (getTag("image") == null) {
            throw new AssertionError("Missing `image` tag for the listing image.");
        }

        // Validate status
        if (getTag("status") == null) {
            throw new AssertionError("Missing `status` tag for the listing status.");
        }
    }

    @Override
    public void validateKind() {
        var n = getKind();
        if (30402 <= n && n <= 30403)
            return;

        throw new AssertionError(String.format("Invalid kind value [%s]. Classified Listing must be either 30402 or 30403", n), null);
    }
}
