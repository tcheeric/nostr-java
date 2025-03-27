package nostr.event.json.codec;

public interface FDecoder<T> {
  
  T decode(String str);
}
