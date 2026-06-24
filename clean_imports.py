import os
import re

ROOT = "./src"

# Speichert alle eigenen Package-Pfade
project_packages = set()

def collect_project_packages():
    for dirpath, _, filenames in os.walk(ROOT):
        for f in filenames:
            if f.endswith(".java"):
                path = os.path.join(dirpath, f)
                with open(path, "r", encoding="utf-8") as file:
                    for line in file:
                        line = line.strip()
                        if line.startswith("package "):
                            pkg = line.replace("package ", "").replace(";", "")
                            project_packages.add(pkg)

def normalize_pkg(pkg: str) -> str:
    return ".".join(p.lower() for p in pkg.split("."))

def is_project_package(pkg: str) -> bool:
    # prüft ob irgendein Prefix im Projekt existiert
    return any(pkg.startswith(p) for p in project_packages)

# 1. Ordner lowercase
def rename_dirs():
    for dirpath, dirnames, _ in os.walk(ROOT, topdown=False):
        for d in dirnames:
            old = os.path.join(dirpath, d)
            new = os.path.join(dirpath, d.lower())

            if old != new:
                if os.path.exists(new):
                    print(f"SKIP exists: {new}")
                    continue
                print(f"DIR: {old} -> {new}")
                os.rename(old, new)

# 2. Java Dateien fixen
def process_file(path):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()

    original = content

    # PACKAGE: nur eigene Pakete
    def pkg_repl(m):
        pkg = m.group(2)
        if is_project_package(pkg):
            return m.group(1) + normalize_pkg(pkg) + m.group(3)
        return m.group(0)

    content = re.sub(
        r"(package\s+)([\w\.]+)(\s*;)",
        pkg_repl,
        content
    )

    # IMPORTS: nur eigene Pakete lowercase, Klassen bleiben
    def import_repl(m):
        full = m.group(2)

        parts = full.split(".")
        pkg_part = ".".join(parts[:-1])
        class_part = parts[-1]

        if is_project_package(pkg_part):
            new_pkg = normalize_pkg(pkg_part)
            return m.group(1) + new_pkg + "." + class_part + m.group(3)

        return m.group(0)

    content = re.sub(
        r"(import\s+)([\w\.\*]+)(\s*;)",
        import_repl,
        content
    )

    if content != original:
        print(f"FILE: {path}")
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)

def walk():
    for dirpath, _, files in os.walk(ROOT):
        for f in files:
            if f.endswith(".java"):
                process_file(os.path.join(dirpath, f))

if __name__ == "__main__":
    collect_project_packages()
    rename_dirs()
    walk()
    print("DONE")