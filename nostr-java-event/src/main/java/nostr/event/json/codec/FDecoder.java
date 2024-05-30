package nostr.event.json.codec;

public interface FDecoder<Filters> {
    Filters decode();
}
