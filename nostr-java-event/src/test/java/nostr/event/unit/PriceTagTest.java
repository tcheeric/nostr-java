package nostr.event.unit;

import nostr.event.tag.PriceTag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceTagTest {
  private static final BigDecimal aVal = new BigDecimal(10.000);
  private static final BigDecimal bVal = new BigDecimal(10.00);
  private static final BigDecimal cVal = new BigDecimal(10.0);
  private static final BigDecimal dVal = new BigDecimal(10.);
  private static final BigDecimal eVal = new BigDecimal(10);

  private static final BigDecimal aString = new BigDecimal("10.000");
  private static final BigDecimal bString = new BigDecimal("10.00");
  private static final BigDecimal cString = new BigDecimal("10.0");
  private static final BigDecimal dString = new BigDecimal("10.");
  private static final BigDecimal eString = new BigDecimal("10");

  private static final String BTC = "BTC";
  private static final String freq = "femptosecond";

  @Test
  void valueParameterCompare() {
    List<PriceTag> list =
        Stream.of(aVal, bVal, cVal, dVal, eVal)
            .map(bigDecimal -> new PriceTag(bigDecimal, BTC, freq))
            .toList();
    assertTrue(list.stream().allMatch(list.getFirst()::equals));
  }

  @Test
  void stringParameterCompare() {
    List<PriceTag> list =
        Stream.of(aString, bString, cString, dString, eString)
            .map(bigDecimal -> new PriceTag(bigDecimal, BTC, freq))
            .toList();
    assertTrue(list.stream().allMatch(list.getFirst()::equals));
  }

  @Test
  void failure() {
    List<PriceTag> priceTags =
        List.of(
            new PriceTag(new BigDecimal("1"), BTC, freq),
            new PriceTag(new BigDecimal("01"), BTC, freq),
            new PriceTag(new BigDecimal("001"), BTC, freq),
            new PriceTag(new BigDecimal(1), BTC, freq),
            new PriceTag(new BigDecimal(01), BTC, freq),
            new PriceTag(new BigDecimal(001), BTC, freq));
    List<PriceTag> list =
        Stream.of(aString, bString, cString, dString, eString)
            .map(bigDecimal -> new PriceTag(bigDecimal, BTC, freq))
            .toList();
    assertTrue(list.stream().noneMatch(priceTags::equals));
  }

  @Test
  void getSupportedFields() {
    PriceTag priceTag = new PriceTag(new BigDecimal(11111), "BTC", "NANOSECONDS");
    assertDoesNotThrow(
        () -> {
          List<Field> list = priceTag.getSupportedFields().stream().toList();
          assertTrue(
              List.of("number", "currency", "frequency")
                  .containsAll(list.stream().map(Field::getName).toList()));
          assertTrue(
              List.of("java.math.BigDecimal", "java.lang.String")
                  .containsAll(
                      list.stream().map(field -> field.getAnnotatedType().toString()).toList()));
        });
  }
}
