package nostr.examples;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import nostr.base.Relay;
import nostr.ws.Connection;

/**
 * @author guilhermegps
 *
 */
public class FilterRelays {
	
	private final static Map<String, String> relaysURLs = Stream.of(new String[][] {
		{ "lightningrelay.com", "lightningrelay.com" },
		{ "nostr.wine", "nostr.wine" },
		{ "at.nostrworks.com", "at.nostrworks.com" },
		{ "brb.io", "brb.io" },
		{ "deschooling.us", "deschooling.us" },
		{ "knostr.neutrine.com", "knostr.neutrine.com" },
		{ "node01.nostress.cc", "node01.nostress.cc" },
		{ "nos.lol", "nos.lol" },
		{ "nostr-01.bolt.observer", "nostr-01.bolt.observer" },
		{ "nostr-1.nbo.angani.co", "nostr-1.nbo.angani.co" },
		{ "nostr1.tunnelsats.com", "nostr1.tunnelsats.com" },
		{ "nostr2.actn.io", "nostr2.actn.io" },
		{ "nostr3.actn.io", "nostr3.actn.io" },
		{ "relay.nostr-latam.link", "relay.nostr-latam.link" },
		{ "nostr.8e23.net", "nostr.8e23.net" },
		{ "nostr.actn.io", "nostr.actn.io" },
		{ "nostr-bg01.ciph.rs", "nostr-bg01.ciph.rs" },
		{ "nostr.bitcoiner.social", "nostr.bitcoiner.social" },
		{ "nostr.blocs.fr", "nostr.blocs.fr" },
		{ "nostr.bostonbtc.com", "nostr.bostonbtc.com" },
		{ "nostr.cercatrova.me", "nostr.cercatrova.me" },
		{ "nostr.coollamer.com", "nostr.coollamer.com" },
		{ "nostr.corebreach.com", "nostr.corebreach.com" },
		{ "no.str.cr", "no.str.cr" },
		{ "nostr.developer.li", "nostr.developer.li" },
		{ "nostr-dev.wellorder.net", "nostr-dev.wellorder.net" },
		{ "nostr.digitalreformation.info", "nostr.digitalreformation.info" },
		{ "nostr.drss.io", "nostr.drss.io" },
		{ "nostr.easydns.ca", "nostr.easydns.ca" },
		{ "nostrex.fly.dev", "nostrex.fly.dev" },
		{ "nostr.f44.dev", "nostr.f44.dev" },
		{ "nostr.fmt.wiz.biz", "nostr.fmt.wiz.biz" },
		{ "nostr.gromeul.eu", "nostr.gromeul.eu" },
		{ "nostr.handyjunky.com", "nostr.handyjunky.com" },
		{ "nostr.hugo.md", "nostr.hugo.md" },
		{ "nostrical.com", "nostrical.com" },
		{ "nostrich.friendship.tw", "nostrich.friendship.tw" },
		{ "nostr.mado.io", "nostr.mado.io" },
		{ "nostr.massmux.com", "nostr.massmux.com" },
		{ "nostr.middling.mydns.jp", "nostr.middling.mydns.jp" },
		{ "nostr.milou.lol", "nostr.milou.lol" },
		{ "nostr.mom", "nostr.mom" },
		{ "nostr.mouton.dev", "nostr.mouton.dev" },
		{ "nostr.mustardnodes.com", "nostr.mustardnodes.com" },
		{ "nostr.mwmdev.com", "nostr.mwmdev.com" },
		{ "nostr.nodeofsven.com", "nostr.nodeofsven.com" },
		{ "nostr.noones.com", "nostr.noones.com" },
		{ "nostr.ownscale.org", "nostr.ownscale.org" },
		{ "nostr.orba.ca", "nostr.orba.ca" },
		{ "no-str.org", "no-str.org" },
		{ "nostr-pub.wellorder.net", "nostr-pub.wellorder.net" },
		{ "nostr.radixrat.com", "nostr.radixrat.com" },
		{ "nostr.rdfriedl.com", "nostr.rdfriedl.com" },
		{ "nostr-relay.alekberg.net", "nostr-relay.alekberg.net" },
		{ "nostrrelay.com", "nostrrelay.com" },
		{ "nostr-relay.derekross.me", "nostr-relay.derekross.me" },
		{ "nostr.relayer.se", "nostr.relayer.se" },
		{ "nostr-relay.gkbrk.com", "nostr-relay.gkbrk.com" },
		{ "nostr-relay.lnmarkets.com", "nostr-relay.lnmarkets.com" },
		{ "nostr-relay.schnitzel.world", "nostr-relay.schnitzel.world" },
		{ "nostr.roundrockbitcoiners.com", "nostr.roundrockbitcoiners.com" },
		{ "nostr.screaminglife.io", "nostr.screaminglife.io" },
		{ "nostr.shawnyeager.net", "nostr.shawnyeager.net" },
		{ "nostr.swiss-enigma.ch", "nostr.swiss-enigma.ch" },
		{ "nostr.uselessshit.co", "nostr.uselessshit.co" },
		{ "nostr-verified.wellorder.net", "nostr-verified.wellorder.net" },
		{ "nostr.vulpem.com", "nostr.vulpem.com" },
		{ "nostr.w3ird.tech", "nostr.w3ird.tech" },
		{ "nostr.yael.at", "nostr.yael.at" },
		{ "nostr.zaprite.io", "nostr.zaprite.io" },
		{ "nostr.zebedee.cloud", "nostr.zebedee.cloud" },
		{ "nostr.zoomout.chat", "nostr.zoomout.chat" },
		{ "paid.no.str.cr", "paid.no.str.cr" },
		{ "private-nostr.v0l.io", "private-nostr.v0l.io" },
		{ "relay.cryptocculture.com", "relay.cryptocculture.com" },
		{ "relay.damus.io", "relay.damus.io" },
		{ "relay.farscapian.com", "relay.farscapian.com" },
		{ "relay.kronkltd.net", "relay.kronkltd.net" },
		{ "relay.lexingtonbitcoin.org", "relay.lexingtonbitcoin.org" },
		{ "relay.mynostr.id", "relay.mynostr.id" },
		{ "relay.nostr.au", "relay.nostr.au" },
		{ "relay.nostr.band", "relay.nostr.band" },
		{ "relay.nostr.bg", "relay.nostr.bg" },
		{ "relay.nostr.express", "relay.nostr.express" },
		{ "relay.nostrgraph.net", "relay.nostrgraph.net" },
		{ "relay.nostrich.de", "relay.nostrich.de" },
		{ "relay.nostrid.com", "relay.nostrid.com" },
		{ "relay.nostr.info", "relay.nostr.info" },
		{ "relay.nostr.nu", "relay.nostr.nu" },
		{ "relay.nostrprotocol.net", "relay.nostrprotocol.net" },
		{ "relay.nostr.ro", "relay.nostr.ro" },
		{ "relay.nostr.scot", "relay.nostr.scot" },
		{ "relay.nostr.vision", "relay.nostr.vision" },
		{ "relay.nvote.co", "relay.nvote.co" },
		{ "relay.nvote.co:443", "relay.nvote.co:443" },
		{ "relay.oldcity-bitcoiners.info", "relay.oldcity-bitcoiners.info" },
		{ "relay-pub.deschooling.us", "relay-pub.deschooling.us" },
		{ "relay.ryzizub.com", "relay.ryzizub.com" },
		{ "nostr-01.dorafactory.org", "nostr-01.dorafactory.org" },
		{ "relay.sendstr.com", "relay.sendstr.com" },
		{ "relay.snort.social", "relay.snort.social" },
		{ "relay.sovereign-stack.org", "relay.sovereign-stack.org" },
		{ "relay.stoner.com", "relay.stoner.com" },
		{ "relay.taxi", "relay.taxi" },
		{ "rsslay.nostr.net", "rsslay.nostr.net" }
	}).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    private final static Set<Relay> relays = new HashSet<>();
	
	public static void main(String[] args) {
    	for (Map.Entry<String,String> r : relaysURLs.entrySet()) 
    		relays.add(updateRelayMetadata(Relay.builder()
    				.name(r.getKey())
    				.uri(r.getValue())
    				.build()));
    	
//    	Filter by NIPs supported
    	var relaysByNips = relays.stream().filter(r -> r.getSupportedNips().containsAll(Arrays.asList(28)))
        		.collect(Collectors.toList());
    	
    	System.out.println(relaysByNips.stream().map(Relay::getUri).collect(Collectors.toList()));
	}

    private static Relay updateRelayMetadata(@NonNull Relay relay) {
        try {
            var connection = new Connection(relay);
            connection.updateRelayMetadata();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return relay;
    }

}
