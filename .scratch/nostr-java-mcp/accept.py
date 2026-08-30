import sys, json, time, subprocess
sys.path.insert(0, '.')
from harness import Server, text

JAR = "/home/eric/IdeaProjects/nostr-java/nostr-java-mcp/target/nostr-java-mcp-2.2.0-runnable.jar"
RELAY = "ws://127.0.0.1:18777"
KS = "/tmp/nostr-mcp-acceptance/keys.p12"
ENV = {"NOSTR_MCP_KEYSTORE_PASSPHRASE": "acceptance-passphrase"}
BASE = ["-Dnostr.mcp.keystore.type=encrypted-file",
        f"-Dnostr.mcp.keystore.path={KS}",
        f"-Dnostr.mcp.relays.read={RELAY}"]

results = []
def check(requirement, ok, evidence):
    results.append((requirement, ok, evidence))
    print(("PASS  " if ok else "FAIL  ") + requirement)
    print("        " + str(evidence)[:200])

def cli(*args):
    import os
    e = dict(os.environ); e.update(ENV)
    return subprocess.run(["java"] + BASE + ["-jar", JAR] + list(args),
                          capture_output=True, text=True, env=e, timeout=120).stdout

# --- Ticket 04: CLI creates keys; keys never printed
out = cli("keygen", "personal")
check("04 CLI keygen creates an identity", "Created identity 'personal'" in out, out.strip().splitlines()[:2])
check("04 CLI never prints the private key", "nsec" not in out, "no nsec in output")
cli("keygen", "project-bot")
check("04 CLI list shows both identities",
      "personal" in cli("list") and "project-bot" in cli("list"), cli("list").split())

# --- Ticket 03/07: unbound multi-identity server
s = Server(JAR, BASE, ENV); s.initialize()
tools = s.tools()
check("02/05/06/07/08/09 full tool surface registered", len(tools) == 22, f"{len(tools)} tools")
ids = s.tool("nostr_list_identities")
check("03 identities listed with public keys only",
      "personal" in json.dumps(ids) and "nsec" not in json.dumps(ids), text(ids)[:120])
check("03 no private key anywhere in the surface",
      not any(k in json.dumps(s.call("tools/list")) for k in ["nsec1", "privateKey"]),
      "swept tools/list for key material")

# --- Ticket 06: ambiguity refused rather than guessed
r = s.tool("nostr_publish_note", {"content": "should not publish"})
check("06 signing refuses to guess between identities",
      r.get("isError") and "IDENTITY_AMBIGUOUS" in text(r), text(r)[:140])

# --- Ticket 06: confirm-then-publish
r1 = s.tool("nostr_publish_note", {"content": "acceptance note", "identity": "personal"})
tok = r1.get("structuredContent", {}).get("confirmationToken")
check("06 first call previews without publishing",
      tok and "Nothing has been published yet" in text(r1), text(r1)[:100])
q = s.tool("nostr_query_events", {"authors": [ids["structuredContent"]["identities"][0]["publicKey"]], "kinds": [1]})
check("06 preview really published nothing", q["structuredContent"]["count"] == 0, text(q))
r2 = s.tool("nostr_publish_note", {"content": "acceptance note", "identity": "personal", "confirmationToken": tok})
check("06 confirmed call publishes", text(r2).startswith("Published "), text(r2)[:100])
check("06 token is single-use",
      s.tool("nostr_publish_note", {"content": "x", "identity": "personal", "confirmationToken": tok}).get("isError"),
      "replay refused")

# --- Ticket 05: read back what we published
time.sleep(1)
pk = ids["structuredContent"]["identities"][0]["publicKey"]
q = s.tool("nostr_query_events", {"authors": [pk], "kinds": [1]})
check("05 published note is queryable", q["structuredContent"]["count"] == 1, text(q))
check("05 npub accepted where hex is",
      s.tool("nostr_query_events", {"authors": [ids["structuredContent"]["identities"][0]["npub"]]})["structuredContent"]["count"] >= 1,
      "npub query matched")
check("05 bad identifier gives a stable code",
      text(s.tool("nostr_get_profile", {"pubkey": "nonsense"})).startswith("INVALID_ARGUMENT"),
      text(s.tool("nostr_get_profile", {"pubkey": "nonsense"}))[:80])

# --- Ticket 05: relative time normalisation
check("05 relative timestamps accepted",
      not s.tool("nostr_query_events", {"authors": [pk], "since": "24h"}).get("isError"), "since=24h accepted")

# --- Ticket 06: profile round trip
s.tool("nostr_update_profile", {"name": "acceptance", "about": "e2e", "identity": "personal"})
tok = s.tool("nostr_update_profile", {"name": "acceptance", "about": "e2e", "identity": "personal"})["structuredContent"]["confirmationToken"]
s.tool("nostr_update_profile", {"name": "acceptance", "about": "e2e", "identity": "personal", "confirmationToken": tok})
time.sleep(1)
p = s.tool("nostr_get_profile", {"pubkey": pk})
check("05/06 profile publishes and decodes", "acceptance: e2e" == text(p), text(p))

# --- Ticket 08: subscriptions
sub = s.tool("nostr_subscribe", {"authors": [pk], "kinds": [1]})
sid = sub["structuredContent"]["subscriptionId"]
check("08 subscribe returns an id without waiting for backlog",
      sid and sub["structuredContent"]["backlogDrained"] is False, text(sub)[:110])
time.sleep(3)
rd = s.tool("nostr_read_subscription", {"subscriptionId": sid})
first = rd["structuredContent"]["count"]
rd2 = s.tool("nostr_read_subscription", {"subscriptionId": sid})
check("08 reading drains, so events are not repeated",
      first >= 1 and rd2["structuredContent"]["count"] == 0, f"first read {first}, second {rd2['structuredContent']['count']}")
check("08 list reports the open subscription", sid in text(s.tool("nostr_list_subscriptions")), text(s.tool("nostr_list_subscriptions"))[:90])
s.tool("nostr_unsubscribe", {"subscriptionId": sid})
check("08 unsubscribed id is then unknown",
      text(s.tool("nostr_read_subscription", {"subscriptionId": sid})).startswith("SUBSCRIPTION_UNKNOWN"), "closed")

# --- Ticket 09: contacts and DM refusal
check("09 contacts reports an absent list as an answer",
      not s.tool("nostr_get_contacts", {"pubkey": pk}).get("isError"), text(s.tool("nostr_get_contacts", {"pubkey": pk}))[:90])
dm = s.tool("nostr_read_direct_messages", {"identity": "personal"})
check("09 DM decryption is off unless enabled", dm.get("isError") and "not enabled" in text(dm), text(dm)[:110])
check("09 no NIP-04 tool is offered", not any("nip04" in t for t in tools), "surface has no nip04 tool")

# --- Ticket 12: prompts and resources
prompts = [p["name"] for p in s.call("prompts/list")["result"]["prompts"]]
check("12 guided prompts are offered",
      prompts == ["compose-note", "catch-up-feed", "watch-mentions"], prompts)
gp = s.call("prompts/get", {"name": "compose-note", "arguments": {"topic": "t"}})
check("12 prompt returns instructions naming the confirmation step",
      "confirmationToken" in json.dumps(gp), "instructions include confirmationToken")
res = [r["uri"] for r in s.call("resources/list")["result"]["resources"]]
check("12 identity and relay resources exposed",
      any("identity" in u for u in res) and any("relay" in u for u in res), res)
s.close()

# --- Ticket 04: bound server
b = Server(JAR, BASE + ["-Dnostr.mcp.identity=personal"], ENV); b.initialize()
bt = b.tools()
bids = b.tool("nostr_list_identities")
check("04 bound server sees only its own identity",
      "project-bot" not in json.dumps(bids) and "personal" in json.dumps(bids), text(bids)[:110])
check("04 bound server registers no keystore-mutating tools",
      not any(t in bt for t in ["nostr_create_identity", "nostr_remove_identity"]), f"{len(bt)} tools, no lifecycle")
check("04 bound server needs no identity argument",
      "identity" not in json.dumps([t for t in b.call("tools/list")["result"]["tools"] if t["name"] == "nostr_publish_note"][0]["inputSchema"]),
      "publish schema has no identity argument")
b.close()

# --- Ticket 06/07: read-only server
d = Server(JAR, BASE + ["-Dnostr.mcp.write-policy=deny"], ENV); d.initialize()
dt = d.tools()
check("06 read-only server registers no write tool", not any("publish" in t for t in dt), f"{len(dt)} tools")
check("07 read-only server cannot mutate the keystore either",
      not any("create_identity" in t or "remove_identity" in t for t in dt), "no lifecycle tools")
d.close()

# --- Ticket 11: environment configuration
e = Server(JAR, BASE, dict(ENV, NOSTR_MCP_WRITE_POLICY="deny")); e.initialize()
check("11 hyphenated settings configurable from the environment",
      not any("publish" in t for t in e.tools()), "NOSTR_MCP_WRITE_POLICY=deny took effect")
e.close()

# --- Ticket 07: bound server refuses a missing identity
import os
env = dict(os.environ); env.update(ENV)
p = subprocess.run(["java"] + BASE + ["-Dnostr.mcp.identity=nonexistent", "-jar", JAR],
                   capture_output=True, text=True, env=env, timeout=90)
check("04 server bound to a missing identity refuses to start",
      p.returncode != 0 and "keygen" in (p.stdout + p.stderr), "refused, naming the fix")

print()
passed = sum(1 for _, ok, _ in results if ok)
print(f"=== {passed}/{len(results)} requirements verified against the shipped jar")
sys.exit(0 if passed == len(results) else 1)
