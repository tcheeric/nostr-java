package nostr.mcp.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.event.impl.GenericEvent;
import nostr.mcp.argument.ToolArguments;
import nostr.mcp.write.WriteGuard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Publishes profile metadata.
 *
 * <p>Kind 0 is replaceable, so publishing one <em>replaces</em> the whole profile rather than
 * amending it: a call that sets only a name erases the existing picture and description. That is
 * a trap for an agent thinking in terms of updating a field, so the tool says so in its
 * description and the preview shows exactly what the profile will become.
 */
public final class UpdateProfileTool extends PublishingTool {

  private static final int PROFILE_KIND = 0;
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final List<String> PROFILE_FIELDS =
      List.of("name", "display_name", "about", "picture", "banner", "website", "nip05", "lud16");

  /**
   * @param writeGuard the point every write passes through
   */
  public UpdateProfileTool(WriteGuard writeGuard) {
    super(writeGuard);
  }

  @Override
  public String name() {
    return "nostr_update_profile";
  }

  @Override
  public String description() {
    return "Replace your public profile metadata. This replaces the whole profile rather than"
        + " editing it, so include every field you want to keep. Read the current profile with"
        + " nostr_get_profile first.";
  }

  @Override
  protected Map<String, Object> writeSpecificProperties() {
    Map<String, Object> properties = new LinkedHashMap<>();
    PROFILE_FIELDS.forEach(
        field -> properties.put(field, Map.of("type", "string", "description", describe(field))));
    return properties;
  }

  @Override
  protected List<String> writeSpecificRequired() {
    return List.of();
  }

  @Override
  protected GenericEvent buildEvent(ToolArguments arguments) {
    Map<String, String> profile = new LinkedHashMap<>();
    PROFILE_FIELDS.forEach(field -> arguments.text(field).ifPresent(value -> profile.put(field, value)));
    if (profile.isEmpty()) {
      throw ToolFailure.INVALID_ARGUMENT.raise(
          "Give at least one profile field. Publishing an empty profile would erase the"
              + " existing one.");
    }
    return GenericEvent.builder()
        .kind(PROFILE_KIND)
        .content(asJson(profile))
        .createdAt(System.currentTimeMillis() / 1000)
        .build();
  }

  private String asJson(Map<String, String> profile) {
    try {
      return MAPPER.writeValueAsString(profile);
    } catch (JsonProcessingException e) {
      throw ToolFailure.INVALID_ARGUMENT.raise("The profile could not be encoded: " + e.getMessage());
    }
  }

  @Override
  protected String describeForPreview(GenericEvent event) {
    return "Your profile would become exactly:\n" + event.getContent();
  }

  private String describe(String field) {
    return switch (field) {
      case "name" -> "Short username.";
      case "display_name" -> "Full display name.";
      case "about" -> "A short biography.";
      case "picture" -> "URL of an avatar image.";
      case "banner" -> "URL of a banner image.";
      case "website" -> "URL of a personal site.";
      case "nip05" -> "A NIP-05 address such as alice@example.com.";
      default -> "A lightning address for receiving zaps.";
    };
  }
}
