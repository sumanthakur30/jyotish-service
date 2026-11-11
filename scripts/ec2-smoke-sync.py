#!/usr/bin/env python3
"""Public sync smoke — prints HTTP status and response keys only."""

from pathlib import Path
import json
import urllib.error
import urllib.request

env = {}
for line in Path("/opt/jyotish/.env").read_text().splitlines():
    if "=" in line and not line.startswith("#"):
        k, v = line.split("=", 1)
        env[k] = v

key = env.get("JYOTISH_LOCAL_API_KEY", "")


def post(path: str, payload: dict, api_key: str, idem: str) -> tuple[int, dict]:
    req = urllib.request.Request(
        f"http://127.0.0.1:8097{path}",
        data=json.dumps(payload).encode(),
        method="POST",
        headers={
            "Content-Type": "application/json",
            "X-Jyotish-Org": "JHAASTRO",
            "X-Jyotish-Api-Key": api_key,
            "Idempotency-Key": idem,
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=20) as res:
            return res.status, json.loads(res.read().decode())
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            payload_out = json.loads(raw)
        except json.JSONDecodeError:
            payload_out = {"raw": raw[:300]}
        return e.code, payload_out


def get(path: str, extra_headers=None):
    headers = extra_headers or {}
    req = urllib.request.Request(f"http://127.0.0.1:8097{path}", headers=headers, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=15) as res:
            return res.status, res.read().decode()[:400]
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()[:400]


health_code, health = get("/actuator/health")
status_code, status = get("/api/v1/jyotish/status")
missing_tenant, missing_body = get("/api/v1/jyotish/clients")
with_tenant, with_tenant_body = get("/api/v1/jyotish/clients", {"X-Tenant-Id": "8801"})

print("health", health_code, health.replace("\n", " ")[:120])
print("status", status_code, status.replace("\n", " ")[:180])
print("clients_no_tenant", missing_tenant)
print("clients_tenant_8801", with_tenant, with_tenant_body.replace("\n", " ")[:160])

bad_code, _ = post(
    "/api/v1/jyotish/public/customers",
    {"name": "X", "mobile": "9000000000"},
    "wrong",
    "evt_test_bad",
)
print("bad_key", bad_code)

cust = {
    "name": "Live Test Rahul",
    "mobile": "9111122233",
    "email": "livetest@example.com",
    "external_customer_id": "JHA-LIVE-CUST-1",
}
c1, r1 = post("/api/v1/jyotish/public/customers", cust, key, "evt_live_cust_1")
c2, r2 = post("/api/v1/jyotish/public/customers", cust, key, "evt_live_cust_1")
print(
    "customer",
    c1,
    r1.get("customer_id"),
    "created",
    r1.get("created"),
    "idem_replay",
    c2,
    r2.get("customer_id"),
    "same",
    r1 == r2,
)

kundli = {
    "name": "Live Test Rahul",
    "mobile": "9111122233",
    "date_of_birth": "1995-08-15",
    "time_of_birth": "10:30:00",
    "birth_place": "Delhi",
    "external_kundli_id": "JHA-LIVE-KUNDLI-1",
}
k_code, k = post("/api/v1/jyotish/public/kundlis", kundli, key, "evt_live_kundli_1")
print(
    "kundli",
    k_code,
    "kundli_id",
    k.get("kundli_id"),
    "person_id",
    k.get("person_id"),
    "msg",
    k.get("message"),
)

booking = {
    "name": "Live Test Rahul",
    "mobile": "9111122233",
    "service_name": "Kundli consultation",
    "booking_date": "2026-09-05",
    "booking_time": "11:00",
    "external_booking_id": "JHA-LIVE-BOOK-1",
}
b_code, b = post("/api/v1/jyotish/public/bookings", booking, key, "evt_live_book_1")
print(
    "booking",
    b_code,
    "booking_id",
    b.get("booking_id"),
    "msg",
    b.get("message"),
)

dash_code, dash = get("/api/v1/jyotish/clients/dashboard", {"X-Tenant-Id": "8801"})
print("dashboard", dash_code, dash.replace("\n", " ")[:200])
