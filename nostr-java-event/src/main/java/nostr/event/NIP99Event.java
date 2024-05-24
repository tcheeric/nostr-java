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
import java.util.function.Function;

@NoArgsConstructor
public abstract class NIP99Event extends GenericEvent {
  Map<String, Function<ClassifiedListing, String>> fxnMap = new HashMap<>();

  Function<ClassifiedListing, String> titleFunction = ClassifiedListing::getTitle;
  Function<ClassifiedListing, String> summaryFunction = ClassifiedListing::getSummary;
  Function<ClassifiedListing, String> publishedAtFunction = e -> e.getPublishedAt().toString();
  Function<ClassifiedListing, String> locationFunction = ClassifiedListing::getLocation;

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

    fxnMap.forEach((attribute, classifiedListingStringFunction) -> extracted(baseTags, classifiedListing, attribute));
  }

  private static void extracted(List<BaseTag> baseTags, ClassifiedListing classifiedListing, String attribute) {
    if (baseTags.stream().noneMatch(tag -> getIsPresent(tag, attribute))) {
      baseTags.add(createTag(attribute, classifiedListing.getSummary()));
    }
  }


  private static GenericTag createTag(String code, String value) {
    return GenericTag.create(code, 99, value);
  }

  private static boolean getIsPresent(BaseTag baseTag, String code) {
    return code.equals(baseTag.getCode());
  }
}
