/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.event.tag.GenericTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author eric
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GenericTagFactory {

    private final String code;
    private final List<String> params;

    private String jsonString;

    protected GenericTagFactory() {
        this.code = "";
        this.params = new ArrayList<>();
    }

    public GenericTagFactory(@NonNull String code, @NonNull List<String> params) {
        this.code = code;
        this.params = params;
    }

    public GenericTagFactory(String code, String... params) {
        this(code, Stream.of(params).filter(param -> param != null).toList());
    }

    public GenericTagFactory(@NonNull String jsonString) {
        this.jsonString = jsonString;
        this.code = "";
        this.params = new ArrayList<>();
    }

    @SneakyThrows
    public GenericTag create() {
        if (jsonString != null) {
            return new ObjectMapper().readValue(jsonString, GenericTag.class);
        }
        return GenericTag.create(code, params);
    }
}
