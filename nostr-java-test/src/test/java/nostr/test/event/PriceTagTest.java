package nostr.test.event;

import nostr.event.tag.PriceTag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriceTagTest {
    private final static BigDecimal aVal = new BigDecimal(10.000);
    private final static BigDecimal bVal = new BigDecimal(10.00);
    private final static BigDecimal cVal = new BigDecimal(10.0);
    private final static BigDecimal dVal = new BigDecimal(10.);
    private final static BigDecimal eVal = new BigDecimal(10);

    private final static BigDecimal aString = new BigDecimal("10.000");
    private final static BigDecimal bString = new BigDecimal("10.00");
    private final static BigDecimal cString = new BigDecimal("10.0");
    private final static BigDecimal dString = new BigDecimal("10.");
    private final static BigDecimal eString = new BigDecimal("10");

    private static final String BTC = "BTC";
    private static final String freq = "femptosecond";

    @Test
    void valueParameterCompare() {
        List<PriceTag> list = Stream.of(
                        aVal, bVal, cVal, dVal, eVal)
                .map(bigDecimal ->
                        new PriceTag(bigDecimal, BTC, freq)).toList();
        assertTrue(list.stream().allMatch(list.getFirst()::equals));
    }

    @Test
    void stringParameterCompare() {
        List<PriceTag> list = Stream.of(
                        aString, bString, cString, dString, eString)
                .map(bigDecimal ->
                        new PriceTag(bigDecimal, BTC, freq)).toList();
        assertTrue(list.stream().allMatch(list.getFirst()::equals));
    }

    @Test
    void failure() {
        List<PriceTag> priceTags = List.of(
                new PriceTag(new BigDecimal("1"), BTC, freq),
                new PriceTag(new BigDecimal("01"), BTC, freq),
                new PriceTag(new BigDecimal("001"), BTC, freq),
                new PriceTag(new BigDecimal(1), BTC, freq),
                new PriceTag(new BigDecimal(01), BTC, freq),
                new PriceTag(new BigDecimal(001), BTC, freq)
        );
        List<PriceTag> list = Stream.of(
                        aString, bString, cString, dString, eString)
                .map(bigDecimal ->
                        new PriceTag(bigDecimal, BTC, freq)).toList();
        assertTrue(list.stream().noneMatch(priceTags::equals));
    }

    @Test
    void getSupportedFields() {
        PriceTag priceTag = new PriceTag(new BigDecimal(11111), "BTC", "NANOSECONDS");
        assertDoesNotThrow(() -> {
            List<Field> list = priceTag.getSupportedFields().stream().toList();
            assertTrue(List.of("number", "currency", "frequency").containsAll(list.stream().map(Field::getName).toList()));
            assertTrue(List.of("java.math.BigDecimal", "java.lang.String").containsAll(list.stream().map(field -> field.getAnnotatedType().toString()).toList()));
        });
    }
    
    @Test
    void donothing() {
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
        System.out.println("00000000000000000000000000000");
    }
}
