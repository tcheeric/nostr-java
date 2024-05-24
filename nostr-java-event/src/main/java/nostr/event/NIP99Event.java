package nostr.event;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@NoArgsConstructor
public abstract class NIP99Event extends GenericEvent {
  Map<String, Function<ClassifiedListing, Optional<String>>> fxnMap = new HashMap<>();

  Function<ClassifiedListing, Optional<String>> titleFunction = e -> Optional.ofNullable(e.getTitle());
  Function<ClassifiedListing, Optional<String>> summaryFunction = e -> Optional.ofNullable(e.getSummary());
  Function<ClassifiedListing, Optional<String>> publishedAtFunction = e -> Optional.ofNullable(e.getPublishedAt()).map(String::valueOf);
  Function<ClassifiedListing, Optional<String>> locationFunction = e -> Optional.ofNullable(e.getLocation());

  public NIP99Event(@NonNull PublicKey pubKey, Kind kind, List<BaseTag> baseTags, @NonNull ClassifiedListing classifiedListing) {
    this(pubKey, kind, baseTags, null, classifiedListing);
  }

  public NIP99Event(@NonNull PublicKey pubKey, Kind kind, List<BaseTag> baseTags, String content, @NonNull ClassifiedListing classifiedListing) {
    super(pubKey, kind);
    setContent(content);

    fxnMap.put("title", titleFunction);
    fxnMap.put("summary", summaryFunction);
    fxnMap.put("published_at", publishedAtFunction);
    fxnMap.put("location", locationFunction);

    fxnMap.forEach((tagCode, tagCodeFunction) -> addNonDuplicateTag(tagCode, classifiedListing, baseTags));
  }

  private void addNonDuplicateTag(String code, ClassifiedListing classifiedListing, List<BaseTag> baseTags) {
    Optional<String> obj = fxnMap.get(code).apply(classifiedListing);
    if (obj.isPresent() && (baseTags.stream().noneMatch(tag -> code.equals(tag.getCode())))) {
      super.addTag(createTag(code, obj.get()));
    }
  }

  private static GenericTag createTag(String code, String value) {
    return GenericTag.create(code, 99, value);
  }
}
