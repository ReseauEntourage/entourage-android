import os
import re

source_dir = "app/src/main/java/social/entourage/android"
files_to_scan = []
for root, dirs, files in os.walk(source_dir):
    for file in files:
        if file.endswith("Fragment.kt"):
            files_to_scan.append(os.path.join(root, file))

count = 0
for file_path in files_to_scan:
    with open(file_path, 'r') as f:
        lines = f.readlines()

    modified = False
    new_lines = []

    # State tracking
    inside_on_view_created = False
    super_calls_seen = 0
    seen_log_events = set()

    # Simple line-by-line processing might be risky if we don't track scope.
    # But usually onViewCreated is a distinct block.

    # Let's iterate.
    # We want to remove the *second* super call if there are two in onViewCreated?
    # Or just remove duplicates globally in the file if they are in the same method?
    # Tracking scope line-by-line is hard without AST.

    # Alternative strategy:
    # 1. Deduplicate consecutive identical lines (for log events).
    # 2. For super.onViewCreated: if we see two in close proximity (like within the same function block), remove one.

    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Check for function start
        if "override fun onViewCreated" in line:
            inside_on_view_created = True
            super_calls_seen = 0
            seen_log_events = set() # Scope dedup for logs? No, user might log different things.
                                    # But we want to dedup IDENTICAL logs.

        if "fun " in line and "onViewCreated" not in line:
            inside_on_view_created = False

        # Dedup Log Events (Consecutive or same block?)
        # Let's just dedup CONSECUTIVE identical non-empty lines, which covers the log dup.
        if i > 0 and stripped == lines[i-1].strip() and "AnalyticsEvents.logEvent" in line:
            # Skip this duplicate line
            i += 1
            modified = True
            continue

        # Handle Super calls in onViewCreated
        if inside_on_view_created and "super.onViewCreated(" in line:
            super_calls_seen += 1
            if super_calls_seen > 1:
                # Remove this second super call
                i += 1
                modified = True
                continue

        new_lines.append(line)
        i += 1

    if modified:
        print(f"Fixed duplicates in {os.path.basename(file_path)}")
        with open(file_path, 'w') as f:
            f.writelines(new_lines)
        count += 1

print(f"Fixed {count} files.")
