package nostr.event.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.event.tag.AddressTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@JsonDeserialize(builder = CalendarContent.CalendarContentBuilder.class)
@EqualsAndHashCode(callSuper = false)
public class CalendarContent extends NIP42Content {
    //@JsonProperty
    //private final String id;

    // below fields mandatory
    private final IdentifierTag identifierTag;
    private final String title;
    private final Long start;

    // below fields optional
    private Long end;
    private String startTzid;
    private String endTzid;
    private String summary;
    private String image;
    private String location;
    private GeohashTag geohashTag;
    private List<PubKeyTag> participantPubKeys;
    private List<LabelTag> labelTags;
    private List<LabelNamespaceTag> labelNamespaceTags;
    private List<HashtagTag> hashtagTags;
    private List<ReferenceTag> referenceTags;
    private List<AddressTag> addressTags;

    public static CalendarContentBuilder builder(@NonNull IdentifierTag identifierTag, @NonNull String title, @NonNull Long start) {
        return new CalendarContentBuilder()
                .identifierTag(identifierTag)
                .title(title)
                .start(start);
    }

    public void addParticipantPubKey(@NonNull PubKeyTag pubKeyTag) {
        if (this.participantPubKeys == null) {
            this.participantPubKeys = new java.util.ArrayList<>();
        }
        this.participantPubKeys.add(pubKeyTag);
    }

    public void addHashtagTag(HashtagTag hashtagTag) {
        if (this.hashtagTags == null) {
            this.hashtagTags = new java.util.ArrayList<>();
        }
        this.hashtagTags.add(hashtagTag);
    }

    public void addReferenceTag(ReferenceTag referenceTag) {
        if (this.referenceTags == null) {
            this.referenceTags = new java.util.ArrayList<>();
        }
        this.referenceTags.add(referenceTag);
    }

    public void addLabelTag(LabelTag labelTag) {
        if (this.labelTags == null) {
            this.labelTags = new java.util.ArrayList<>();
        }
        this.labelTags.add(labelTag);
    }

    public void addLabelNamespaceTag(LabelNamespaceTag labelNamespaceTag) {
        if (this.labelNamespaceTags == null) {
            this.labelNamespaceTags = new ArrayList<>();
        }
        this.labelNamespaceTags.add(labelNamespaceTag);
    }

    public void addAddressTag(AddressTag addressTag) {
        if (this.addressTags == null) {
            this.addressTags = new java.util.ArrayList<>();
        }
        this.addressTags.add(addressTag);
    }
}
