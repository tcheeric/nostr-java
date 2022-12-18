/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event.tag;

import com.tcheeric.nostr.base.annotation.Key;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.base.annotation.Tag;
import nostr.event.BaseTag;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@Tag(code = "subject")
@ToString
@NIPSupport(14)
public final class SubjectTag extends BaseTag {

    @Key
    private String subject;

    public SubjectTag(@NonNull String subject) {
        this.subject = subject;
    }
}
