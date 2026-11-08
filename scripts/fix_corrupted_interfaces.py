# -*- coding: utf-8 -*-
"""Fix Java interface files corrupted with `public interface X , ,` syntax."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "backend"
PATTERN = re.compile(r"^public interface (\w+) , ,\s*$")


def main() -> None:
    fixed = 0
    for path in ROOT.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        if ", ," not in text:
            continue
        lines = text.splitlines(keepends=True)
        new_lines: list[str] = []
        changed = False
        for line in lines:
            match = PATTERN.match(line.rstrip("\r\n"))
            if match:
                name = match.group(1)
                new_lines.append(f"public interface {name} {{\n")
                new_lines.append("\n")
                new_lines.append("}\n")
                changed = True
            else:
                new_lines.append(line)
        if changed:
            path.write_text("".join(new_lines), encoding="utf-8", newline="\n")
            fixed += 1
            print(path)
    print(f"Fixed {fixed} files")


if __name__ == "__main__":
    main()
