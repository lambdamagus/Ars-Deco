#!/usr/bin/env python3
"""Validate Ars Deco PNG textures and report corrupted files."""

from __future__ import annotations

import argparse
import pathlib
import struct
import sys
import zlib


PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
REPLACEMENT_BYTES = b"\xef\xbf\xbd"


def validate_png(path: pathlib.Path) -> tuple[bool, str, int]:
    data = path.read_bytes()
    replacement_count = data.count(REPLACEMENT_BYTES)

    if data[:8] != PNG_SIGNATURE:
        return False, "bad PNG signature", replacement_count

    offset = len(PNG_SIGNATURE)
    while offset + 12 <= len(data):
        length = struct.unpack(">I", data[offset : offset + 4])[0]
        chunk_type = data[offset + 4 : offset + 8]
        chunk_start = offset + 8
        chunk_end = chunk_start + length
        crc_end = chunk_end + 4

        if crc_end > len(data):
            return False, f"truncated chunk {chunk_type!r}", replacement_count

        expected_crc = struct.unpack(">I", data[chunk_end:crc_end])[0]
        actual_crc = zlib.crc32(chunk_type + data[chunk_start:chunk_end]) & 0xFFFFFFFF
        if expected_crc != actual_crc:
            chunk_name = chunk_type.decode("latin1", errors="replace")
            return False, f"bad CRC in {chunk_name}", replacement_count

        offset = crc_end
        if chunk_type == b"IEND":
            return True, "valid PNG", replacement_count

    return False, "missing IEND chunk", replacement_count


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate PNG textures and list corrupted files.")
    parser.add_argument(
        "root",
        nargs="?",
        default="src/main/resources/assets/ars_deco/textures",
        help="Texture root to scan. Defaults to Ars Deco texture assets.",
    )
    parser.add_argument(
        "--report",
        default=None,
        help="Optional markdown report path to write.",
    )
    args = parser.parse_args()

    root = pathlib.Path(args.root)
    if not root.exists():
        print(f"Texture root does not exist: {root}", file=sys.stderr)
        return 2

    good: list[pathlib.Path] = []
    bad: list[tuple[pathlib.Path, str, int]] = []

    for path in sorted(root.rglob("*.png")):
        ok, message, replacement_count = validate_png(path)
        if ok:
            good.append(path)
        else:
            bad.append((path, message, replacement_count))

    print(f"Scanned: {len(good) + len(bad)} PNG files")
    print(f"Valid: {len(good)}")
    print(f"Corrupted: {len(bad)}")

    if bad:
        print()
        print("Corrupted files:")
        for path, message, replacement_count in bad:
            print(f"- {path.as_posix()} ({message}; replacement byte sequences: {replacement_count})")

    if args.report:
        report_path = pathlib.Path(args.report)
        with report_path.open("w", encoding="utf-8") as report:
            report.write("# Texture Validation Report\n\n")
            report.write(f"Scanned: {len(good) + len(bad)} PNG files\n\n")
            report.write(f"Valid: {len(good)}\n\n")
            report.write(f"Corrupted: {len(bad)}\n\n")
            if bad:
                report.write("## Corrupted Files\n\n")
                for path, message, replacement_count in bad:
                    report.write(
                        f"- `{path.as_posix()}` - {message}; "
                        f"replacement byte sequences: {replacement_count}\n"
                    )
        print()
        print(f"Wrote report: {report_path}")

    return 1 if bad else 0


if __name__ == "__main__":
    raise SystemExit(main())
