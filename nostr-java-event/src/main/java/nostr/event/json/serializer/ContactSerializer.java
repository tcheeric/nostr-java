package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nostr.event.impl.CustomerOrderEvent.Customer.Contact;

import java.io.IOException;

/**
 * @author eric
 */
public class ContactSerializer extends JsonSerializer<Contact> {

  @Override
  public void serialize(Contact contact, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
    jsonGenerator.writeStartObject();
    jsonGenerator.writeStringField("nostr", contact.getPublicKey().toString());
    jsonGenerator.writeStringField("phone", contact.getPhone());
    jsonGenerator.writeStringField("email", contact.getEmail());
    jsonGenerator.writeEndObject();
  }

}
