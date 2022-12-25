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

    public String printSupportedNips() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        final List<Integer> supportedNipList = this.getSupportedNips();

        sb.append("[");
        for (int n : supportedNipList) {

            sb.append(n);

            if (i++ < supportedNipList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");

        return sb.toString();
    }

}
