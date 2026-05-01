---
name: tycho-build-and-target-platform
description: Guidance for Tycho, target-platform, p2, feature, category, and Eclipse dependency/build alignment.
---

# Tycho Build and Target Platform

Use this skill when working on tasks related to:
- `.target` files
- Tycho or Maven build configuration
- target-platform alignment
- p2 repositories
- installable units
- `feature.xml`
- `category.xml`
- dependency resolution in Eclipse plugin builds
- execution environment alignment
- bundle availability on the supported Eclipse baseline

## Goal

Keep the build reproducible and compatible with the repository's minimum supported:
- Eclipse target-platform
- JDK
- bundle dependency set

## Working Approach

### 1. Treat target-platform as the source of truth
When deciding whether a dependency, plugin, feature, or API can be used, first check whether it is available and compatible in the configured target-platform.

Do not assume a dependency is valid simply because it exists in Maven Central or works in a plain Java project.

### 2. Keep Tycho and metadata aligned
When build-related changes are made, check the consistency among:
- `.target` files
- parent and module `pom.xml`
- Tycho plugins and configuration
- `META-INF/MANIFEST.MF`
- `feature.xml`
- `category.xml`
- p2 repository definitions
- execution environment or compiler target settings

A correct solution usually requires these files to remain aligned, not just one of them.

### 3. Minimize dependency expansion
When adding dependencies:
- add only what is necessary
- prefer dependencies already available in the current target-platform
- avoid broadening bundle requirements unnecessarily
- avoid introducing optional complexity unless there is a clear need

Be careful with transitive assumptions. Tycho and OSGi resolution are stricter than plain Maven dependency resolution.

### 4. Respect execution environment and JDK boundaries
Do not change Java execution environment, compiler target level, or required JDK assumptions unless explicitly requested.

If the task requires a newer Java level or newer platform baseline, call that out clearly instead of silently upgrading constraints.

### 5. Feature and p2 consistency
If the repository publishes features, update sites, or p2 metadata:
- ensure newly required bundles are reflected where needed
- ensure features include the right plugins and fragments
- ensure category definitions remain valid
- keep installable content and runtime requirements synchronized

Do not update only the plugin dependency while forgetting the publishable artifacts.

### 6. Prefer stable build changes
For Tycho-related updates:
- prefer minimal version changes
- avoid unnecessary plugin upgrades
- avoid broad build modernization unless explicitly requested
- keep build configuration understandable and repository-consistent

### 7. Diagnose resolution issues carefully
When build failures are related to Tycho or target-platform, consider:
- missing bundles in the target-platform
- mismatched bundle versions
- execution environment mismatch
- missing feature inclusion
- wrong p2 repository or category content
- `Require-Bundle` vs `Import-Package` implications
- platform-specific fragments or native dependencies
- test bundles requiring additional runtime content

## Build Alignment Checklist

Before considering a build-related task complete, check:
- dependency exists in the configured target-platform
- dependency is compatible with the minimum supported Eclipse version
- dependency is compatible with the minimum supported JDK
- `MANIFEST.MF` is aligned with actual usage
- `.target` files are still valid
- `pom.xml` and Tycho config remain consistent
- `feature.xml` is updated if installable content changed
- `category.xml` is updated if published content changed
- p2 repository definitions remain correct
- no unnecessary platform baseline upgrade was introduced

## Output Expectations

When proposing or implementing Tycho or target-platform changes, include relevant notes about:
- why the dependency or bundle is expected to resolve in the configured target-platform
- whether `MANIFEST.MF`, `.target`, `pom.xml`, `feature.xml`, or `category.xml` also need updates
- whether the task changes the supported platform or JDK baseline
- any risk to reproducible builds, p2 publishing, or installable content