package nostr.event.tag;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.base.ElementAttribute;
import nostr.base.IGenericElement;
import nostr.event.BaseTag;
import nostr.event.json.serializer.GenericTagSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonSerialize(using = GenericTagSerializer.class)
public class GenericTag extends BaseTag implements IGenericElement {

	private final String code;

	private final List<ElementAttribute> attributes;

	public GenericTag(String code) {
		this(code, new ArrayList<>());
	}

	/**
	 * nip parameter to be removed
	 *
	 * @deprecated use any available proper constructor variant instead
	 */
	@Deprecated(forRemoval = true)
	public GenericTag(String code, Integer nip) {
		this(code, new ArrayList<>());
	}

	public GenericTag(@NonNull String code, @NonNull ElementAttribute... attribute) {
		this(code, List.of(attribute));
	}

	public GenericTag(@NonNull String code, @NonNull List<ElementAttribute> attributes) {
		this.code = code;
		this.attributes = attributes;
	}

	@Override
	public void addAttribute(@NonNull ElementAttribute... attribute) {
		this.addAttributes(List.of(attribute));
	}

	@Override
	public void addAttributes(@NonNull List<ElementAttribute> attributes) {
		this.attributes.addAll(attributes);
	}

	/**
	 * nip parameter to be removed
	 *
	 * @deprecated use {@link #create(String, String...)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static GenericTag create(String code, Integer nip, String... params) {
		return create(code, List.of(params));
	}

	/**
	 * nip parameter to be removed
	 *
	 * @deprecated use {@link #create(String, List)} instead.
	 */

	@Deprecated(forRemoval = true)
	public static GenericTag create(String code, Integer nip, List<String> params) {
		return create(code, params);
	}

	public static GenericTag create(@NonNull String code, @NonNull String... params) {
		return create(code, List.of(params));
	}

	public static GenericTag create(@NonNull String code, @NonNull List<String> params) {
		return new GenericTag(code,
				IntStream.range(0, params.size())
						.mapToObj(i ->
										  new ElementAttribute("param".concat(String.valueOf(i)), params.get(i)))
						.toList());
	}
}
