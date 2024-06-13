package nostr.test.event;

import nostr.event.tag.PriceTag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceTagTest {
  @Test
  void getSupportedFields() {
    PriceTag priceTag = new PriceTag(new BigDecimal(11111), "BTC", "NANOSECONDS");
    assertDoesNotThrow(() -> {
      List<Field> list = priceTag.getSupportedFields().stream().toList();
      assertTrue(List.of("number", "currency", "frequency").containsAll(list.stream().map(Field::getName).toList()));
      assertTrue(List.of("java.math.BigDecimal", "java.lang.String").containsAll(list.stream().map(field -> field.getAnnotatedType().toString()).toList()));
    });
  }
}