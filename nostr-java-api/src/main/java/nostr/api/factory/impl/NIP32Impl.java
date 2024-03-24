/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.TagFactory;

import java.util.Map;

import static nostr.util.NostrUtil.escapeJsonString;

/**
 *
 * @author eric
 */
public class NIP32Impl {

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class NamespaceTagFactory extends TagFactory {

        public NamespaceTagFactory(@NonNull NameSpace nameSpace) {
            super("L", 32, nameSpace.getValue());
        }
    }

    public static class LabelTagFactory extends TagFactory {

        public LabelTagFactory(@NonNull Label label) {
            super("l", 32, label.toParams());
        }

    }

    @Data
    @AllArgsConstructor
    public static class NameSpace {

        private final String value;
    }

    @Data
    @AllArgsConstructor
    public static class Label {

        private final NameSpace nameSpace;
        private final String value;
        private Map<String, Object> metadata;

        public Label(NameSpace nameSpace, String value) {
            this(nameSpace, value, null);
        }

        public String[] toParams() {
            try {
                String[] result;
                if (metadata != null) {
                    result = new String[3];
                    result[0] =  value;
                    result[1] = nameSpace.getValue();
                    result[2] = escapeJsonString(new ObjectMapper().writeValueAsString(metadata));
                } else {
                    result = new String[2];
                    result[0] =  value;
                    result[1] = nameSpace.getValue();
                }

                return result;
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

    }
}
