"""Fetch the unmodified official SDK at an immutable commit and verify its bytes."""
import hashlib
import pathlib
import urllib.request

COMMIT = '999b1143a6bff0dd79db11d21b92c15c56a346b1'
SHA256 = 'be00ee9cd7115f6b11984c6e31fe98e298fb726940d1555063610685ef3bbf29'
target = pathlib.Path(__file__).resolve().parents[1] / 'app/libs/glyph-matrix-sdk-2.0.aar'
url = f'https://raw.githubusercontent.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit/{COMMIT}/glyph-matrix-sdk-2.0.aar'
data = target.read_bytes() if target.exists() else urllib.request.urlopen(url, timeout=60).read()
if hashlib.sha256(data).hexdigest() != SHA256:
    raise SystemExit('Official SDK checksum mismatch')
target.parent.mkdir(parents=True, exist_ok=True)
target.write_bytes(data)
print('Official Nothing Glyph Matrix SDK 2.0 checksum verified')
