#!/usr/bin/env python3
"""
Generates content/places/sns-urgencias.json from the SNS Transparency Portal dataset
"Caracterização das Valências de Urgência" (one row per service line; we keep one entry
per hospital unit, with its highest urgency level).

Usage:  python tools/data/sns_urgencias.py [content-dir]
Only the standard library is used. Phone numbers and e-mails are deliberately NOT copied
(rule: no phone number without a checked source).

Coordinates are NOT copied either: on 2026-11-21, 68 of the 94 units had several different
coordinates across rows, some hundreds of km apart (Marco 8 report). Address, postal code
and locality were consistent, so "open in maps" searches by address instead.
"""
import datetime
import json
import re
import sys
import unicodedata
import urllib.request

DATASET = "caracterizacao-das-valencias-de-urgencia"
API = f"https://transparencia.sns.gov.pt/api/explore/v2.1/catalog/datasets/{DATASET}"

# Highest first. Names exactly as in the dataset.
LEVELS = {
    "Serviço de Urgência Polivalente com Centro de Trauma": "polyvalent-trauma",
    "Serviço de Urgência Polivalente": "polyvalent",
    "Serviço de Urgência Médico-cirúrgico": "medical-surgical",
    "Serviço de Urgência Básica": "basic",
}
RANK = {v: i for i, v in enumerate(LEVELS.values())}


def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": "resilia-content-script"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def slug(text):
    t = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode().lower()
    return re.sub(r"[^a-z0-9]+", "-", t).strip("-")


def main(content_dir):
    meta = get(API)
    modified = meta["metas"]["default"]["modified"][:10]
    rows = get(f"{API}/exports/json")
    units = {}
    for r in rows:
        name = (r.get("unidade_hospitalar") or "").strip()
        level = LEVELS.get((r.get("tipo_de_urgencia") or "").strip())
        if not name or level is None:
            continue
        u = units.setdefault(name, {
            "id": slug(name),
            "name": name,
            "group": (r.get("entidade_grupo_hospitalar") or "").strip(),
            "urgency": level,
            "address": (r.get("endereco") or "").strip(),
            "postalCode": (r.get("codigo_postal") or "").strip(),
            "locality": (r.get("localidade") or "").strip(),
            "region": (r.get("regiao") or "").strip(),
        })
        if RANK[level] < RANK[u["urgency"]]:
            u["urgency"] = level
    out = {
        "sourceIds": ["sns-urgencias"],
        "dataModified": modified,
        "retrievedAt": datetime.date.today().isoformat(),
        "hospitals": sorted(units.values(), key=lambda u: u["id"]),
        "review": {"status": "draft"},
    }
    ids = [u["id"] for u in out["hospitals"]]
    assert len(ids) == len(set(ids)), "duplicate ids"
    path = f"{content_dir}/places/sns-urgencias.json"
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"{len(ids)} hospitals, dataset modified {modified} -> {path}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "content")
