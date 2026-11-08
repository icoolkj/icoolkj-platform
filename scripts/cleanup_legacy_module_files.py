#!/usr/bin/env python3
"""Move leftover feature-folder Java files into flat system layers; remove old XML dirs."""
from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SYSTEM = ROOT / "backend" / "icoolkj-admin-system" / "src" / "main" / "java" / "com" / "icoolkj" / "platform" / "system"
MAPPER_RES = ROOT / "backend" / "icoolkj-admin-system" / "src" / "main" / "resources" / "mapper"
PKG = "com.icoolkj.platform.system"
LAYERS = {"domain", "mapper", "service", "dto", "vo", "convert", "support"}


def classify(name: str) -> str:
    if name.endswith("Entity.java"):
        return "domain"
    if name.endswith("Mapper.java"):
        return "mapper"
    if name.endswith("ServiceImpl.java") or name.endswith("QueryServiceImpl.java") or name.endswith("CacheServiceImpl.java") or name.endswith("PolicyServiceImpl.java") or name.endswith("MatchServiceImpl.java"):
        return "service/impl"
    if name in ("LoginSecurityConfigService.java", "TenantConfigResolver.java"):
        return "service/impl"
    if name.endswith("Service.java") or name.endswith("QueryService.java") or name.endswith("CacheService.java") or name.endswith("PolicyService.java") or name.endswith("MatchService.java"):
        return "service"
    if name.endswith("Request.java") or name.endswith("PageQuery.java"):
        return "dto"
    if name.endswith("VO.java") or name == "RouteVO.java":
        return "vo"
    if name.endswith("Convert.java"):
        return "convert"
    if name.endswith("TreeBuilder.java"):
        return "support"
    return ""


def move_legacy_java() -> int:
    moved = 0
    for mod in list(SYSTEM.iterdir()):
        if not mod.is_dir() or mod.name in LAYERS or mod.name == "service":
            continue
        for java_file in list(mod.rglob("*.java")):
            sub = classify(java_file.name)
            if not sub:
                continue
            dst_dir = SYSTEM.joinpath(*sub.split("/"))
            dst_dir.mkdir(parents=True, exist_ok=True)
            dst = dst_dir / java_file.name
            content = java_file.read_text(encoding="utf-8")
            # normalize package
            new_pkg = f"{PKG}.{sub.replace('/', '.')}"
            content = re.sub(r"^package [\w.]+;", f"package {new_pkg};", content, count=1, flags=re.M)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.domain\.", f"{PKG}.domain.", content)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.mapper\.", f"{PKG}.mapper.", content)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.service\.impl", f"{PKG}.service.impl", content)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.service", f"{PKG}.service", content)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.dto", f"{PKG}.dto", content)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.vo", f"{PKG}.vo", content)
            content = re.sub(r"com\.icoolkj\.platform\.system\.\w+\.convert", f"{PKG}.convert", content)
            if dst.exists() and dst.resolve() != java_file.resolve():
                java_file.unlink()
            else:
                dst.write_text(content, encoding="utf-8")
                if java_file.resolve() != dst.resolve():
                    java_file.unlink()
            moved += 1
        for p in sorted(mod.rglob("*"), reverse=True):
            if p.is_dir() and not any(p.iterdir()):
                p.rmdir()
        if mod.is_dir() and not any(mod.iterdir()):
            mod.rmdir()
    return moved


def remove_old_xml() -> int:
    n = 0
    for d in list(MAPPER_RES.iterdir()):
        if d.is_dir() and d.name != "system":
            shutil.rmtree(d)
            n += 1
    return n


if __name__ == "__main__":
    m = move_legacy_java()
    x = remove_old_xml()
    print(f"moved/removed java: {m}, removed xml dirs: {x}")
