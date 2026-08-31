"""Drives the shipped jar as an MCP host does, over stdio, against a real relay."""
import json, subprocess, sys, threading, time, itertools

class Server:
    def __init__(self, jar, props, env=None):
        import os
        e = dict(os.environ); e.update(env or {})
        self.p = subprocess.Popen(
            ["java"] + props + ["-jar", jar],
            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
            text=True, bufsize=1, env=e)
        self.ids = itertools.count(1)

    def call(self, method, params=None, notify=False):
        msg = {"jsonrpc": "2.0", "method": method}
        if params is not None: msg["params"] = params
        if not notify:
            msg["id"] = next(self.ids)
        self.p.stdin.write(json.dumps(msg) + "\n"); self.p.stdin.flush()
        if notify: return None
        while True:
            line = self.p.stdout.readline()
            if not line: raise RuntimeError("server closed the stream")
            try: reply = json.loads(line)
            except Exception: continue
            if reply.get("id") == msg["id"]: return reply

    def initialize(self):
        self.call("initialize", {"protocolVersion": "2024-11-05", "capabilities": {},
                                 "clientInfo": {"name": "acceptance", "version": "1"}})
        self.call("notifications/initialized", notify=True)

    def tools(self):
        return [t["name"] for t in self.call("tools/list")["result"]["tools"]]

    def tool(self, name, args=None):
        return self.call("tools/call", {"name": name, "arguments": args or {}})["result"]

    def close(self):
        self.p.terminate()
        try: self.p.wait(timeout=10)
        except Exception: self.p.kill()

def text(result):
    for c in result.get("content", []):
        if c.get("type") == "text": return c["text"]
    return ""
