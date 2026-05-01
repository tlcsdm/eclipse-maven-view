# Copilot Instructions

## General Working Rules

This repository is used for Eclipse plugin related development.  
When generating or modifying code, configuration, tests, build files, or documentation, always follow these rules.

### 1. Compatibility first
- Always prioritize compatibility with the repository's minimum supported Eclipse target-platform.
- Always prioritize compatibility with the repository's minimum supported JDK version.
- Do not assume the latest Eclipse APIs or latest Java language features are available.
- If compatibility is unclear, prefer the more conservative implementation.

### 2. Follow the existing repository style
- Reuse the existing architecture, naming, module boundaries, package structure, and file organization.
- Prefer the frameworks and implementation patterns already used in the repository.
- Avoid introducing new frameworks, libraries, or architectural styles unless explicitly requested.

### 3. Keep changes minimal and focused
- Make the smallest safe change that solves the task.
- Avoid unrelated refactoring, broad renaming, or formatting-only edits across unrelated files.
- Preserve backward compatibility wherever possible.

### 4. Commit messages
Use Angular Commit Convention for all suggested commit messages.

Examples:
- `feat(ui): add preference page`
- `fix(core): avoid null pointer on startup`
- `docs(readme): clarify installation steps`
- `build(target-platform): align dependency constraints`
- `refactor(service): simplify project resolver`
- `test(ui): stabilize wizard interaction test`
- `ci: Improve github workflow`
- `chore: clean up unused scripts`
- `style: format code with prettier`

### 5. Dependency discipline
- Do not add dependencies unless necessary.
- Any new dependency must be compatible with the repository's minimum supported Eclipse target-platform and minimum supported JDK.
- Prefer stable, public, well-supported APIs over internal or experimental ones.

### 6. Explain important assumptions
When making non-trivial changes:
- state important assumptions clearly
- mention compatibility considerations
- mention related metadata or configuration files that may also need updates
- call out risks if internal APIs or version-sensitive features are involved

## Skill routing
When a task involves Eclipse plugin specific work, also apply the appropriate skill guidance from:

- `.github/skills/eclipse-plugin-development/SKILLS.md`
- `.github/skills/tycho-build-and-target-platform/SKILLS.md`
- `.github/skills/eclipse-ui-development/SKILLS.md`
- `.github/skills/eclipse-plugin-testing/SKILLS.md`

### Skill selection guidance
- Use `eclipse-plugin-development` for general plugin, OSGi, extension-point, manifest, lifecycle, and integration work.
- Use `tycho-build-and-target-platform` for target-platform, Tycho, p2, feature, category, dependency resolution, and build alignment tasks.
- Use `eclipse-ui-development` for SWT, JFace, commands, handlers, views, editors, dialogs, wizards, preference pages, and workbench UI tasks.
- Use `eclipse-plugin-testing` for JUnit, PDE, Tycho test execution, SWTBot, UI test stability, and plugin/runtime test structure.