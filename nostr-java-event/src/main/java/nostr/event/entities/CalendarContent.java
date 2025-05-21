package nostr.event.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import nostr.event.tag.AddressTag;
import nostr.event.tag.GeohashTag;
import nostr.event.tag.HashtagTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.LabelTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.ReferenceTag;

@EqualsAndHashCode(callSuper = false)
public class CalendarContent<T extends BaseTag> extends NIP42Content {
    //@JsonProperty
    //private final String id;

    // below fields mandatory
    @Getter
    private final IdentifierTag identifierTag;
    @Getter
    private final String title;
    @Getter
    private final Long start;

    // below fields optional
    private Long end;
    private String startTzid;
    private String endTzid;
    private String summary;
    private String image;
    private String location;
    private Map<String, List<T>> classTypeTagsMap = new HashMap<>();

    public CalendarContent(@NonNull IdentifierTag identifierTag, @NonNull String title, @NonNull Long start) {
        this.identifierTag = identifierTag;
        this.title = title;
        this.start = start;
    }

    public void setEnd(@NonNull Long end) {
        this.end = end;
    }

    public Optional<Long> getEnd() {
        return Optional.ofNullable(this.end);
    }

    public void setStartTzid(@NonNull String startTzid) {
        this.startTzid = startTzid;
    }

    public Optional<String> getStartTzid() {
        return Optional.ofNullable(this.startTzid);
    }

    public void setEndTzid(@NonNull String endTzid) {
        this.endTzid = endTzid;
    }

    public Optional<String> getEndTzid() {
        return Optional.ofNullable(this.endTzid);
    }

    public void setSummary(@NonNull String summary) {
        this.summary = summary;
    }

    public Optional<String> getSummary() {
        return Optional.ofNullable(this.summary);
    }

    public void setImage(@NonNull String image) {
        this.image = image;
    }

    public Optional<String> getImage() {
        return Optional.ofNullable(this.image);
    }

    public void setLocation(@NonNull String location) {
        this.location = location;
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(this.location);
    }

    public void addParticipantPubKeyTag(@NonNull PubKeyTag pubKeyTag) {
        addTag((T) pubKeyTag);
    }

    public void addParticipantPubKeyTags(@NonNull List<PubKeyTag> pubKeyTags) {
        pubKeyTags.forEach(this::addParticipantPubKeyTag);
    }

    public List<PubKeyTag> getParticipantPubKeyTags() {
        return getTagsByType(PubKeyTag.class).stream()
            .toList();
    }

    public void addHashtagTag(@NonNull HashtagTag hashtagTag) {
        addTag((T) hashtagTag);
    }

    public void addHashtagTags(@NonNull List<HashtagTag> hashtagTags) {
        hashtagTags.forEach(this::addHashtagTag);
    }

    public List<HashtagTag> getHashtagTags() {
        return getTagsByType(HashtagTag.class);
    }

    public void addReferenceTag(@NonNull ReferenceTag referenceTag) {
        addTag((T) referenceTag);
    }

    public List<ReferenceTag> getReferenceTags() {
        return getTagsByType(ReferenceTag.class);
    }

    public void addLabelTag(@NonNull LabelTag labelTag) {
        addTag((T) labelTag);
    }

    public void addLabelTags(@NonNull List<LabelTag> labelTags) {
        labelTags.forEach(this::addLabelTag);
    }

    public List<LabelTag> getLabelTags() {
        return getTagsByType(LabelTag.class);
    }

    public void addLabelNamespaceTag(@NonNull LabelNamespaceTag labelNamespaceTag) {
        addTag((T) labelNamespaceTag);
    }

    public void addLabelNamespaceTags(@NonNull List<LabelNamespaceTag> labelNamespaceTags) {
        labelNamespaceTags.forEach(this::addLabelNamespaceTag);
    }

    public List<LabelNamespaceTag> getLabelNamespaceTags() {
        return getTagsByType(LabelNamespaceTag.class);
    }

    public void addAddressTag(@NonNull AddressTag addressTag) {
        addTag((T) addressTag);
    }

    public List<AddressTag> getAddressTags() {
        return getTagsByType(AddressTag.class);
    }

    public void setGeohashTag(@NonNull GeohashTag geohashTag) {
        addTag((T) geohashTag);
    }

    public Optional<GeohashTag> getGeohashTag() {
        return getTagsByType(GeohashTag.class).stream().findFirst();
    }

    private <T extends BaseTag> List<T> getTagsByType(Class<T> clazz) {
        Tag annotation = clazz.getAnnotation(Tag.class);
        List<T> list = getBaseTags(annotation).stream()
            .map(clazz::cast)
            .toList();
        return list;
    }

    private List<T> getBaseTags(@NonNull Tag type) {


        String code = type.code();
        List<T> value = classTypeTagsMap.get(code);
        Optional<List<T>> value1 = Optional.ofNullable(value);
        List<T> baseTags = value1.orElse(Collections.emptyList());
        return (List<T>) baseTags;
    }

    private void addTag(@NonNull T baseTag) {
        String code = baseTag.getCode();
        Optional<List<T>> optionalBaseTags = Optional.ofNullable(classTypeTagsMap.get(code));
        List<T> baseTags = optionalBaseTags.orElseGet(ArrayList::new);
        baseTags.add(baseTag);
//            .ifPresent(list -> list.add(baseTag));
//            .orElse(classTypeTagsMap.put(code, new ArrayList<>()))
        classTypeTagsMap.put(code, baseTags);
        List<T> baseTags1 = classTypeTagsMap.get(code);
        baseTags1.addAll(baseTags);
    }
}

