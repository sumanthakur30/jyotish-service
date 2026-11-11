#!/usr/bin/env python3
"""Merge shop RDS credentials into /opt/jyotish/.env and ensure schema jyotish exists."""

from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path


SHOP = "sumanthakur30-shop-service-1"
ENV_PATH = Path("/opt/jyotish/.env")


def denv(name: str) -> str:
    return subprocess.check_output(["docker", "exec", SHOP, "printenv", name], text=True).strip()


def parse_jdbc(url: str) -> tuple[str, str, str]:
    rest = url.replace("jdbc:postgresql://", "", 1)
    hostport, _, dbq = rest.partition("/")
    host, _, port = hostport.partition(":")
    dbname, _, _q = dbq.partition("?")
    return host, port or "5432", dbname


def existing_keep(path: Path) -> dict[str, str]:
    keep = {
        "JYOTISH_LOCAL_API_KEY",
        "JYOTISH_SSO_SECRET",
        "JYOTISH_ORG",
        "SERVER_PORT",
    }
    out: dict[str, str] = {}
    if not path.exists():
        return out
    for line in path.read_text().splitlines():
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        if key in keep:
            out[key] = value
    return out


def main() -> int:
    url = denv("SPRING_DATASOURCE_URL")
    user = denv("SPRING_DATASOURCE_USERNAME")
    password = denv("SPRING_DATASOURCE_PASSWORD")
    host, port, _db = parse_jdbc(url)
    kept = existing_keep(ENV_PATH)

    rows = {
        "SERVER_PORT": kept.get("SERVER_PORT", "8097"),
        "SPRING_PROFILES_ACTIVE": "",
        "EUREKA_CLIENT_ENABLED": "false",
        "JYOTISH_ENTITLEMENT_ENABLED": "false",
        "JYOTISH_EPHEMERIS_PROVIDER": "MEEUS",
        "JYOTISH_DB_SCHEMA": "jyotish",
        "SPRING_DATASOURCE_URL": url,
        "SPRING_DATASOURCE_USERNAME": user,
        "SPRING_DATASOURCE_PASSWORD": password,
        "JYOTISH_LOCAL_API_KEY": kept.get("JYOTISH_LOCAL_API_KEY", ""),
        "JYOTISH_SSO_SECRET": kept.get("JYOTISH_SSO_SECRET", ""),
        "JYOTISH_ORG": kept.get("JYOTISH_ORG", "JHAASTRO"),
        "JYOTISH_ORG_TENANT_JHAASTRO": "8801",
        "JAVA_TOOL_OPTIONS": "-Xms128m -Xmx320m -XX:+ExitOnOutOfMemoryError",
    }
    ENV_PATH.parent.mkdir(parents=True, exist_ok=True)
    os.umask(0o077)
    ENV_PATH.write_text("".join(f"{k}={v}\n" for k, v in rows.items()), encoding="utf-8")
    os.chmod(ENV_PATH, 0o600)

    env = os.environ.copy()
    env["PGPASSWORD"] = password
    conn = f"host={host} port={port} user={user} dbname=shopdb sslmode=require"
    created = subprocess.run(
        ["psql", conn, "-v", "ON_ERROR_STOP=1", "-c", "CREATE SCHEMA IF NOT EXISTS jyotish;"],
        env=env,
        capture_output=True,
        text=True,
    )
    if created.returncode != 0:
        sys.stderr.write("schema create failed\n")
        sys.stderr.write(created.stderr)
        return created.returncode
    print("env-updated schema-ok")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
