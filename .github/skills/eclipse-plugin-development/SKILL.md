---
name: eclipse-plugin-development
description: Guidance for implementing and modifying Eclipse plugin, RCP, and OSGi-based code while preserving target-platform and JDK compatibility.
---

# Eclipse Plugin Development

Use this skill when working on tasks related to:

- Eclipse plugins
- Eclipse RCP applications
- OSGi bundles
- extension points
- plugin metadata
- target-platform constrained implementation
- PDE / Tycho based plugin projects
- SWT / JFace / Eclipse UI integration

## Goal

Produce changes that fit Eclipse plugin development conventions and remain compatible with:
- the repository's minimum supported Eclipse target-platform
- the repository's minimum supported JDK version

## Working Approach

### 1. Start from repository constraints
Before suggesting implementation details, first align with the repository's actual setup if it can be determined from the codebase, build files, or target-platform:
- minimum supported Eclipse release
- target-platform definition
- minimum supported JDK / execution environment
- Tycho / Maven / PDE build structure
- current plugin and module layout
- existing UI framework usage such as SWT, JFace, e4, commands, handlers, views, editors, or wizards

If exact compatibility boundaries are unclear, prefer the most conservative implementation that is likely to work on the currently configured minimum versions.

### 2. Follow Eclipse-native architecture
Prefer solutions that align with existing Eclipse and OSGi patterns already used by the repository, such as:
- extension points
- commands and handlers
- views, editors, dialogs, and wizards
- preference pages and preference stores
- background jobs and progress monitors
- OSGi services or declarative services if already present
- Eclipse workspace and resource APIs where appropriate

Do not replace Eclipse-native mechanisms with generic alternatives unless explicitly requested.

### 3. Keep plugin metadata synchronized
When implementation changes affect plugin wiring, runtime behavior, packaging, or dependencies, check whether related metadata must also be updated.

Common files to inspect:
- `META-INF/MANIFEST.MF`
- `plugin.xml`
- `fragment.xml`
- `build.properties`
- `feature.xml`
- `category.xml`
- `.target` files
- `pom.xml`
- product configuration files
- service component descriptors
- preference initializer classes
- plugin activator or lifecycle related files

Examples:
- adding a handler, menu contribution, command, view, editor, or preference page may require `plugin.xml`
- adding or removing bundle dependencies may require `MANIFEST.MF`
- adding packaged resources may require `build.properties`
- changing installable content may require `feature.xml`, `category.xml`, or p2-related configuration
- changing platform assumptions may require updates to `.target` files or Tycho configuration

### 4. Prefer public APIs
Use stable public Eclipse APIs whenever possible.

Avoid packages containing `.internal.` unless:
- the repository already intentionally depends on them, and
- no suitable public API exists

If internal API use is unavoidable, explicitly call out the compatibility and maintenance risk.

### 5. Respect UI and threading constraints
For SWT/JFace/Eclipse UI work:
- access UI widgets on the UI thread
- avoid blocking the UI thread with long-running work
- use background jobs for expensive operations
- report progress when appropriate
- keep handlers, dialogs, wizards, views, and editors consistent with existing repository patterns

Do not introduce Swing, JavaFX, or browser/web UI approaches unless they are already part of the repository or explicitly requested.

### 6. Respect internationalization practices
If the repository externalizes user-facing strings:
- do not hardcode visible UI text
- update the relevant messages or properties files
- follow the existing NLS pattern consistently

### 7. Use Eclipse-friendly error handling
When appropriate, prefer patterns already common in Eclipse plugin codebases, such as:
- `IStatus`
- `Status`
- `MultiStatus`
- `CoreException`

Use the repository's existing logging and status reporting style where available.  
Avoid `printStackTrace` and avoid silent failure paths.

### 8. Preserve stable IDs and contracts
Unless explicitly requested, do not rename or break the stability of:
- plugin IDs
- bundle symbolic names
- extension point IDs
- command IDs
- handler IDs
- view IDs
- editor IDs
- preference keys
- marker IDs
- builder IDs
- nature IDs
- exported packages
- public APIs

Backward compatibility is especially important for plugin integrations and workspace metadata.

### 9. Use version-safe Java
Do not use Java language features, standard library APIs, or build assumptions beyond the repository's minimum supported JDK.

If there is any doubt, prefer the older and more broadly compatible implementation.

Be especially careful with features that are version-sensitive, for example:
- records
- sealed classes
- pattern matching
- enhanced switch syntax
- text blocks
- virtual threads
- preview features
- newer collection APIs

### 10. Keep tests aligned with plugin realities
When adding or updating tests:
- follow the repository's existing test structure
- stay consistent with current PDE / Tycho / JUnit / SWTBot usage if present
- keep tests compatible with the minimum supported platform and JDK
- avoid fragile UI timing assumptions
- avoid tests that depend on environment-specific state unless the repository already uses such patterns intentionally

## Eclipse Plugin Task Checklist

Before considering a task complete, check whether the change should also verify or update:
- API compatibility with the minimum Eclipse target-platform
- API compatibility with the minimum JDK
- `META-INF/MANIFEST.MF`
- `plugin.xml`
- `build.properties`
- feature / p2 / category metadata
- `.target` or Tycho/Maven configuration
- externalized strings and message bundles
- background job vs UI thread behavior
- logging and error reporting consistency
- public IDs and extension contracts

## Output Expectations

When proposing or implementing changes for Eclipse plugin tasks, include relevant notes about:
- why the solution is expected to work with the minimum target-platform
- why the solution is expected to work with the minimum JDK
- which plugin metadata files may also need to be updated
- any compatibility risk introduced by API or dependency choices