package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.entities.CashuToken;

public class CashuTokenSerializer extends JsonSerializer<CashuToken> {

    @Override
    public void serialize(CashuToken value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws java.io.IOException {
        jsonGenerator.writeStartObject();

        // Write the mint field
        jsonGenerator.writeStringField("mint", value.getMint().getUrl());

        // Write the proofs array
        jsonGenerator.writeArrayFieldStart("proofs");
        for (var proof : value.getProofs()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("id", proof.getId());
            jsonGenerator.writeNumberField("amount", proof.getAmount());
            jsonGenerator.writeStringField("secret", proof.getSecret());
            jsonGenerator.writeStringField("C", proof.getC());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        // Write the del array if not empty
        if (!value.getDestroyed().isEmpty()) {
            jsonGenerator.writeArrayFieldStart("del");
            for (var destroyed : value.getDestroyed()) {
                jsonGenerator.writeString(destroyed);
            }
            jsonGenerator.writeEndArray();
        }

        jsonGenerator.writeEndObject();
    }
}
