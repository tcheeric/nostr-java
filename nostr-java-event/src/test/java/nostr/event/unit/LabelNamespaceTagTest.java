package nostr.event.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.base.ElementAttribute;
import nostr.event.tag.LabelNamespaceTag;
import nostr.event.tag.GenericTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LabelNamespaceTagTest {

    @Test
    void testSerialization() throws Exception {
        LabelNamespaceTag tag = new LabelNamespaceTag("namespace");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(tag);
        assertTrue(json.contains("\"L\",\"namespace\""), "Serialized JSON should contain the namespace. " + json);
    }

    @Test
    void testDeserialization() throws Exception {
        String json = "[\"L\", \"namespace\"]";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);
        LabelNamespaceTag tag = LabelNamespaceTag.deserialize(node);
        assertEquals("namespace", tag.getNameSpace(), "Deserialized namespace should match the JSON value");
    }

    @Test
    void testUpdateFields() {
        GenericTag genericTag = new GenericTag("L", new ArrayList<>());
        genericTag.addAttribute(new ElementAttribute("param0", "namespace"));
        LabelNamespaceTag tag = LabelNamespaceTag.updateFields(genericTag);
        assertEquals("namespace", tag.getNameSpace(), "Updated namespace should match the generic tag attribute");
    }
}