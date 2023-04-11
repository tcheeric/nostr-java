package nostr.event.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.extern.java.Log;
import nostr.types.values.impl.ExpressionValue;

/**
 * @author guilhermegps
 *
 */
@Log
public class ExpressionValueSerializer extends StdSerializer<ExpressionValue> {
	
	protected ExpressionValueSerializer() {
		super(ExpressionValue.class);
	}

	private static final long serialVersionUID = -2885584373177082299L;

	@Override
	public void serialize(ExpressionValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
