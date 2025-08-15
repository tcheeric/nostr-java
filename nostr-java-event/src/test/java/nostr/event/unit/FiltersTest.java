package nostr.event.unit;

import nostr.base.Kind;
import nostr.event.filter.Filterable;
import nostr.event.filter.Filters;
import nostr.event.filter.KindFilter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FiltersTest {

    @Test
    void missingTypeReturnsEmptyList() {
        Filters filters = new Filters(new KindFilter<>(Kind.valueOf(1)));
        assertTrue(filters.getFilterByType("unknown").isEmpty());
    }

    @Test
    void setLimitRequiresPositive() {
        Filters filters = new Filters(new KindFilter<>(Kind.valueOf(1)));
        assertThrows(IllegalArgumentException.class, () -> filters.setLimit(0));
        assertThrows(IllegalArgumentException.class, () -> filters.setLimit(-5));
        filters.setLimit(1);
        assertEquals(1, filters.getLimit());
    }

    @Test
    void nullFilterKeyThrows() throws Exception {
        Map<String, List<Filterable>> map = new HashMap<>();
        map.put(null, List.of(new KindFilter<>(Kind.valueOf(1))));
        Constructor<Filters> constructor = Filters.class.getDeclaredConstructor(Map.class);
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> constructor.newInstance(map));
        assertEquals("Filter key for filterable [kinds] is not defined", ex.getCause().getMessage());
    }
}
