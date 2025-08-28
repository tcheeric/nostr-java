package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

public interface Encoder {
  ObjectMapper ENCODER_MAPPER_BLACKBIRD =
      JsonMapper.builder()
          .addModule(new BlackbirdModule())
          .build()
          .setSerializationInclusion(Include.NON_NULL);

  String encode();
}
