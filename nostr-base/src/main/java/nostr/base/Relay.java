/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.base;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
public class Relay {

    private final String uri;

    private String name;

    @ToString.Exclude
    private String description;

    @ToString.Exclude
    private byte[] pubKey;

    @ToString.Exclude
    private String contact;

    @Builder.Default
    @ToString.Exclude
    private List<Integer> supportedNips = new ArrayList<>();

    @ToString.Exclude
    private String software;

    @ToString.Exclude
    private String version;
}
