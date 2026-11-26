#!/usr/bin/env python3
"""Deterministic local-only HTTP fixtures for MER-0.1 consumer-boundary tests."""
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from threading import Event
import time


cancel_started = Event()


class FixtureHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == "/probe.json":
            self.send_payload(200, b'{"status":"ok","source":"local-fixture"}')
        elif self.path == "/status/503":
            self.send_payload(503, b'{"error":"fixture unavailable"}')
        elif self.path == "/malformed.json":
            self.send_payload(200, b'{"status":')
        elif self.path == "/empty.json":
            self.send_payload(200, b'')
        elif self.path == "/partial.json":
            self.send_payload(200, b'{"status":"ok"}')
        elif self.path == "/slow":
            self.send_delayed_payload(200, b'{"status":"ok","source":"local-fixture"}', 0.35)
        elif self.path == "/cancel":
            cancel_started.set()
            time.sleep(3)
            self.send_payload(200, b'{"status":"ok","source":"local-fixture"}')
        elif self.path == "/cancel/reset":
            cancel_started.clear()
            self.send_payload(200, b'{"status":"reset"}')
        elif self.path == "/cancel/started":
            if cancel_started.is_set():
                self.send_payload(200, b'{"status":"started"}')
            else:
                self.send_payload(404, b'{"status":"pending"}')
        else:
            self.send_payload(404, b'{"error":"not found"}')

    def send_payload(self, status, body):
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        try:
            self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            # Expected when the client correctly cancels/times out the slow fixture.
            pass

    def send_delayed_payload(self, status, body, delay_seconds):
        # Deliver headers first so cancellation tests can synchronize on an
        # established HTTP response while the body is still in flight.
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.flush()
        time.sleep(delay_seconds)
        try:
            self.wfile.write(body)
        except (BrokenPipeError, ConnectionResetError):
            # Expected when the client correctly cancels/times out the slow fixture.
            pass

    def log_message(self, format, *args):
        print("%s - %s" % (self.address_string(), format % args), flush=True)


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 8765), FixtureHandler).serve_forever()
