package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.event.BaseTag;
import nostr.util.NostrException;
import org.apache.commons.lang3.stream.Streams;

import java.io.IOException;

import static nostr.event.json.codec.BaseTagEncoder.BASETAG_ENCODER_MAPPED_AFTERBURNER;

public abstract class AbstractTagSerializer<T extends BaseTag> extends StdSerializer<T> {
	protected AbstractTagSerializer(Class<T> t) {
		super(t);
	}

	public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) {
		try {
			final ObjectNode node = BASETAG_ENCODER_MAPPED_AFTERBURNER.getNodeFactory().objectNode();
			Streams.failableStream(value.getSupportedFields().stream()).forEach(f -> node.put(f.getName(), value.getFieldValue(f)));

			processNode(value);

			ArrayNode arrayNode = node.objectNode().putArray("values").add(value.getCode());
			node.fields().forEachRemaining(entry -> arrayNode.add(entry.getValue().asText()));
			gen.writePOJO(arrayNode);
		} catch (IOException | NostrException e) {
			throw new RuntimeException(e);
		}
	}

	public void processNode(T value) {}
}
