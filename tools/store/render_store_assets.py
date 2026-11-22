#!/usr/bin/env python3
"""
Renders the Google Play store images from the app's own vector shapes (ic_launcher_foreground):
  - icon-512.png          512 x 512, the high-res icon (Play asks for 512 x 512 PNG)
  - feature-graphic.png   1024 x 500, the feature graphic

Usage: python tools/store/render_store_assets.py [out-dir]   (needs Pillow)
The name and tagline come from arguments so the app name stays in one place in the code.
"""
import re
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
TEAL = (0x0F, 0x5E, 0x63)
WARM = (0xFB, 0xFA, 0xF6)
WINDOW = (0xF6, 0xDF, 0xA4)
SS = 4  # supersampling for anti-aliasing

SHIELD = "M54,25 C60,28.5 69,31 77,31.5 L77,51 C77,66 67.5,77.5 54,83 C40.5,77.5 31,66 31,51 L31,31.5 C39,31 48,28.5 54,25 Z"
HOUSE = ("M54,40 L68,51.5 C68.8,52.2 68.3,53.5 67.3,53.5 L65,53.5 L65,65 C65,66.1 64.1,67 63,67 L45,67 "
         "C43.9,67 43,66.1 43,65 L43,53.5 L40.7,53.5 C39.7,53.5 39.2,52.2 40,51.5 Z")
WINDOW_PATH = ("M50.5,55 L57.5,55 C58.1,55 58.5,55.4 58.5,56 L58.5,61 C58.5,61.6 58.1,62 57.5,62 L50.5,62 "
               "C49.9,62 49.5,61.6 49.5,61 L49.5,56 C49.5,55.4 49.9,55 50.5,55 Z")


def polygon(path, scale, dx=0.0, dy=0.0, steps=24):
    """Flattens an M/L/C/Z path (absolute) into points."""
    tokens = re.findall(r"[MLCZ]|-?\d+(?:\.\d+)?", path)
    pts, i, cur, cmd = [], 0, (0.0, 0.0), None
    while i < len(tokens):
        t = tokens[i]
        if t in "MLCZ":
            cmd = t
            i += 1
            if t == "Z":
                continue
        if cmd in ("M", "L"):
            cur = (float(tokens[i]), float(tokens[i + 1]))
            pts.append(cur)
            i += 2
        elif cmd == "C":
            c1 = (float(tokens[i]), float(tokens[i + 1]))
            c2 = (float(tokens[i + 2]), float(tokens[i + 3]))
            end = (float(tokens[i + 4]), float(tokens[i + 5]))
            for k in range(1, steps + 1):
                u = k / steps
                a, b, c, d = (1 - u) ** 3, 3 * (1 - u) ** 2 * u, 3 * (1 - u) * u ** 2, u ** 3
                pts.append((a * cur[0] + b * c1[0] + c * c2[0] + d * end[0],
                            a * cur[1] + b * c1[1] + c * c2[1] + d * end[1]))
            cur = end
            i += 6
    return [(x * scale + dx, y * scale + dy) for x, y in pts]


def draw_symbol(draw, scale, dx, dy):
    draw.polygon(polygon(SHIELD, scale, dx, dy), fill=WARM)
    draw.polygon(polygon(HOUSE, scale, dx, dy), fill=TEAL)
    draw.polygon(polygon(WINDOW_PATH, scale, dx, dy), fill=WINDOW)


def icon(out):
    size = 512 * SS
    img = Image.new("RGB", (size, size), TEAL)
    # The 108-unit adaptive canvas mapped to the full square (Play applies its own mask).
    draw_symbol(ImageDraw.Draw(img), size / 108, 0, 0)
    img.resize((512, 512), Image.LANCZOS).save(out / "icon-512.png")


def feature(out, name, tagline):
    w, h = 1024 * SS, 500 * SS
    img = Image.new("RGB", (w, h), TEAL)
    d = ImageDraw.Draw(img)
    scale = h / 108 * 1.05
    draw_symbol(d, scale, 40 * SS - 18 * scale, -2 * SS)
    font_dir = ROOT / "core/designsystem/src/main/res/font"
    big = ImageFont.truetype(str(font_dir / "atkinson_hyperlegible_bold.ttf"), 112 * SS)
    small = ImageFont.truetype(str(font_dir / "atkinson_hyperlegible_regular.ttf"), 40 * SS)
    x = 470 * SS
    d.text((x, 150 * SS), name, font=big, fill=WARM)
    y = 300 * SS
    for line in tagline.split("|"):
        d.text((x, y), line, font=small, fill=WARM)
        y += 54 * SS
    img.resize((1024, 500), Image.LANCZOS).save(out / "feature-graphic.png")


if __name__ == "__main__":
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "docs/store"
    name = sys.argv[2] if len(sys.argv) > 2 else "App"
    tagline = sys.argv[3] if len(sys.argv) > 3 else "Tagline"
    out.mkdir(parents=True, exist_ok=True)
    icon(out)
    feature(out, name, tagline)
    print("written to", out)
