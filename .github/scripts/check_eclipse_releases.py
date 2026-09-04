#!/usr/bin/env python3
"""
Script to check for new Eclipse platform releases and update target files and workflows.
Eclipse releases quarterly: March (03), June (06), September (09), December (12).
"""

import os
import re
import sys
from datetime import datetime
from pathlib import Path
import urllib.request


RELEASE_VERSION_URL_PATTERNS = [
    r'(https://mirrors\.aliyun\.com/eclipse/releases/)(\d{4}-\d{2})(/)',
    r'(https://download\.eclipse\.org/tools/orbit/simrel/orbit-aggregation/)(\d{4}-\d{2})(/?)',
]


def get_eclipse_release_versions():
    """
    Generate list of Eclipse release versions based on quarterly release schedule.
    Returns versions from 2024-06 onwards up to 1 year in the future.
    """
    start_year = 2024
    start_month = 6  # Start from 2024-06

    # Calculate end date (current year + 1 year)
    current_year = datetime.now().year
    end_year = current_year + 1

    versions = []
    months = [3, 6, 9, 12]  # March, June, September, December

    for year in range(start_year, end_year + 1):
        for month in months:
            # Skip versions before 2024-06
            if year == start_year and month < start_month:
                continue
            version = f"{year}-{month:02d}"
            versions.append(version)

    return versions


def check_eclipse_release_exists(version):
    """
    Check if an Eclipse release exists by trying to access the repository URL.
    Uses the Aliyun mirror as it's used in the existing target files.
    """
    url = f"https://mirrors.aliyun.com/eclipse/releases/{version}/"
    try:
        req = urllib.request.Request(url, method='HEAD')
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status == 200
    except Exception as e:
        print(f"  Release {version} not available: {e}")
        return False


def get_existing_targets():
    """Get list of existing target versions from the targets directory."""
    targets_dir = Path("targets")
    if not targets_dir.exists():
        return []

    target_files = list(targets_dir.glob("*.target"))
    versions = []

    for target_file in target_files:
        # Extract version from filename (e.g., 2024-06.target -> 2024-06)
        match = re.match(r'(\d{4}-\d{2})\.target', target_file.name)
        if match:
            versions.append(match.group(1))

    return sorted(versions)


def get_latest_target_file():
    """Get the path to the latest existing target file."""
    existing_versions = get_existing_targets()
    if not existing_versions:
        return None

    latest_version = sorted(existing_versions)[-1]
    return Path(f"targets/{latest_version}.target")


def create_target_file(version):
    """Create a new target file for the given Eclipse version based on the latest existing target."""
    # Get the latest existing target file to use as a template
    latest_target = get_latest_target_file()

    if latest_target is None or not latest_target.exists():
        print("✗ No existing target file found to use as template")
        return None

    # Read the latest target file
    target_content = latest_target.read_text(encoding='utf-8')

    # Replace all known repository URLs that use Eclipse release version in the path
    for pattern in RELEASE_VERSION_URL_PATTERNS:
        target_content = re.sub(pattern, rf'\g<1>{version}\g<3>', target_content)

    # Write the new target file
    target_file = Path(f"targets/{version}.target")
    target_file.write_text(target_content, encoding='utf-8')
    print(f"✓ Created target file: {target_file} (based on {latest_target.name})")
    return target_file


def main():
    """Main function to check for new Eclipse releases and update files."""
    print("Checking for new Eclipse platform releases...")
    print()

    # Get existing targets
    existing_versions = get_existing_targets()
    print(f"Existing target versions: {', '.join(existing_versions)}")
    print()

    # Get potential Eclipse versions
    potential_versions = get_eclipse_release_versions()

    # Find new versions that don't have target files yet
    new_versions = [v for v in potential_versions if v not in existing_versions]

    if not new_versions:
        print("✓ All known Eclipse releases already have target files.")
        # Set output for GitHub Actions
        if 'GITHUB_OUTPUT' in os.environ:
            with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
                f.write("changes=false\n")
        else:
            print("Note: GITHUB_OUTPUT not set (local testing)")
        return 0

    print(f"Checking {len(new_versions)} potential new releases...")
    print()

    # Check which new versions actually exist
    available_new_versions = []
    for version in new_versions:
        print(f"Checking Eclipse {version}...")
        if check_eclipse_release_exists(version):
            print(f"  ✓ Eclipse {version} is available!")
            available_new_versions.append(version)
        else:
            print(f"  ✗ Eclipse {version} is not available yet")

    print()

    if not available_new_versions:
        print("✓ No new Eclipse releases found.")
        # Set output for GitHub Actions
        if 'GITHUB_OUTPUT' in os.environ:
            with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
                f.write("changes=false\n")
        else:
            print("Note: GITHUB_OUTPUT not set (local testing)")
        return 0

    # Process the oldest available version first
    # This ensures we add releases incrementally and don't skip any
    new_version = sorted(available_new_versions)[0]  # Take the oldest new version

    print(f"Adding support for Eclipse {new_version}...")
    print()

    # Create target file
    try:
        create_target_file(new_version)
    except Exception as e:
        print(f"✗ Failed to create target file: {e}")
        return 1

    print()
    print(f"✓ Successfully added support for Eclipse {new_version}")
    print()
    print("Changes:")
    print(f"  - Created targets/{new_version}.target")

    # Set output for GitHub Actions
    if 'GITHUB_OUTPUT' in os.environ:
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write("changes=true\n")
            f.write(f"new_version={new_version}\n")
    else:
        print()
        print("Note: GITHUB_OUTPUT not set (local testing)")

    return 0


if __name__ == "__main__":
    sys.exit(main())
