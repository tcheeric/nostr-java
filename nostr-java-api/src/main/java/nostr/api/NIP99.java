package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.factory.impl.GenericTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.id.Identity;

import java.net.URL;
import java.util.List;

import static nostr.api.NIP12.createGeohashTag;
import static nostr.api.NIP12.createHashtagTag;
import static nostr.api.NIP23.createImageTag;
import static nostr.api.NIP23.createPublishedAtTag;
import static nostr.api.NIP23.createSummaryTag;
import static nostr.api.NIP23.createTitleTag;

public class NIP99 extends EventNostr {

    public NIP99(@NonNull Identity sender) {
        setSender(sender);
    }

    public NIP99 createClassifiedListingEvent(@NonNull List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.CLASSIFIED_LISTING, baseTags, content).create();

        genericEvent.addTag(createTitleTag(classifiedListing.getTitle()));
        genericEvent.addTag(createSummaryTag(classifiedListing.getSummary()));

        if (classifiedListing.getPublishedAt() != null) {
            genericEvent.addTag(createPublishedAtTag(classifiedListing.getPublishedAt()));
        }

        if (classifiedListing.getLocation() != null) {
            genericEvent.addTag(createLocationTag(classifiedListing.getLocation()));
        }

        genericEvent.addTag(classifiedListing.getPriceTag());

        updateEvent(genericEvent);

        return this;
    }

    public static GenericTag createLocationTag(@NonNull String location) {
        return new GenericTagFactory(Constants.Tag.LOCATION_CODE, location).create();
    }

    public static GenericTag createPriceTag(@NonNull String price, @NonNull String currency) {
        return new GenericTagFactory(Constants.Tag.PRICE_CODE, price, currency, null).create();
    }

    public static GenericTag createPriceTag(@NonNull String price, @NonNull String currency, String frequency) {
        return new GenericTagFactory(Constants.Tag.PRICE_CODE, price, currency, frequency).create();
    }

    public static GenericTag createStatusTag(@NonNull String status) {
        return new GenericTagFactory(Constants.Tag.STATUS_CODE, status).create();
    }

    public NIP99 addHashtagTag(@NonNull String hashtag) {
        getEvent().addTag(createHashtagTag(hashtag));
        return this;
    }

    public NIP99 addLocationTag(@NonNull String location) {
        getEvent().addTag(createLocationTag(location));
        return this;
    }

    public NIP99 addGeohashTag(@NonNull String geohash) {
        getEvent().addTag(createGeohashTag(geohash));
        return this;
    }

    public NIP99 addPriceTag(@NonNull String price, @NonNull String currency, String frequency) {
        getEvent().addTag(createPriceTag(price, currency, frequency));
        return this;
    }

    public NIP99 addPriceTag(@NonNull String price, @NonNull String currency) {
        return addPriceTag(price, currency, null);
    }

    public NIP99 addTitleTag(@NonNull String title) {
        getEvent().addTag(createTitleTag(title));
        return this;
    }

    public NIP99 addSummaryTag(@NonNull String summary) {
        getEvent().addTag(createSummaryTag(summary));
        return this;
    }

    public NIP99 addPublishedAtTag(@NonNull Long date) {
        getEvent().addTag(createPublishedAtTag(date));
        return this;
    }

    public NIP99 addImageTag(@NonNull URL url, String size) {
        getEvent().addTag(createImageTag(url, size));
        return this;
    }

    public NIP99 addStatusTag(@NonNull String status) {
        getEvent().addTag(createStatusTag(status));
        return this;
    }

    public NIP99 addTag(@NonNull BaseTag tag) {
        getEvent().addTag(tag);
        return this;
    }
}
