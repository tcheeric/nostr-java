/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event.impl;

import com.tcheeric.nostr.base.NostrException;
import com.tcheeric.nostr.base.Profile;
import com.tcheeric.nostr.event.Kind;
import com.tcheeric.nostr.base.PublicKey;
import com.tcheeric.nostr.event.list.TagList;
import com.tcheeric.nostr.json.JsonType;
import com.tcheeric.nostr.json.JsonValue;
import com.tcheeric.nostr.json.values.JsonExpression;
import com.tcheeric.nostr.json.values.JsonObjectValue;
import com.tcheeric.nostr.json.values.JsonStringValue;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.tcheeric.nostr.base.annotation.NIPSupport;
import com.tcheeric.nostr.json.values.JsonValueList;

/**
 *
 * @author squirrel
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NIPSupport(description = "Basic Event Kinds: set_metadata")
public final class MetadataEvent extends GenericEvent {

    private static final String NAME_PATTERN = "\\w[\\w\\-]+\\w";

    private Profile profile;

    public MetadataEvent(PublicKey pubKey, TagList tagList, Profile profile) throws NostrException, NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        super(pubKey, Kind.SET_METADATA, tagList);
        this.profile = profile;

        this.validate();
    }

    private void validate() throws NostrException {
        var valid = this.profile.getName().matches(NAME_PATTERN);
        if (!valid) {
            throw new NostrException("Invalid profile name: " + this.profile);
        }
    }

    @Override
    public void update() throws NoSuchAlgorithmException, IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, NostrException {
        setContent();
        
        super.update();
    }

    private void setContent() {        
        JsonValue nameValue = new JsonStringValue(this.getProfile().getName());
        JsonExpression<JsonType> nameExpr = JsonExpression.builder().variable("name").jsonValue(nameValue).build();

        JsonValue aboutValue = new JsonStringValue(this.getProfile().getAbout());
        JsonExpression<JsonType> aboutExpr = JsonExpression.builder().variable("about").jsonValue(aboutValue).build();

        JsonValue picValue = new JsonStringValue(this.getProfile().getPicture().toString());
        JsonExpression<JsonType> picExpr = JsonExpression.builder().variable("picture").jsonValue(picValue).build();

        JsonValueList value = new JsonValueList();
        value.add(nameExpr);
        value.add(aboutExpr);
        value.add(picExpr);

        JsonObjectValue content = new JsonObjectValue(value);
        setContent(content.toString(true));
    }

}
