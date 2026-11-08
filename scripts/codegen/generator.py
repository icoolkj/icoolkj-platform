#!/usr/bin/env python3
"""兼容入口 — 转发到 tools/icoolkj-codegen/codegen.py"""
from __future__ import annotations

import runpy
import sys
from pathlib import Path

TARGET = Path(__file__).resolve().parents[2] / "tools" / "icoolkj-codegen" / "codegen.py"
sys.argv[0] = str(TARGET)
runpy.run_path(str(TARGET), run_name="__main__")
