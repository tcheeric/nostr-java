package nostr.event.json.codec;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nostr.base.Encoder;
import nostr.event.filter.Filters;

public record FiltersEncoder(Filters filters) implements Encoder {

	@Override
	public String encode() {
		ObjectNode root = ENCODER_MAPPED_AFTERBURNER.createObjectNode();

		filters.getFiltersMap().forEach((key, filterableList) -> {
			final ObjectNode objectNode = ENCODER_MAPPED_AFTERBURNER.createObjectNode();
			root.setAll(
					filterableList
							.stream()
							.map(filterable ->
										 filterable.toObjectNode(objectNode))
							.toList()
							.getFirst());
		});

		return root.toString();
	}
}
