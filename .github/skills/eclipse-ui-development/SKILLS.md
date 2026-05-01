---
name: eclipse-ui-development
description: Guidance for SWT, JFace, commands, handlers, views, editors, dialogs, wizards, and Eclipse workbench UI integration.
---

# Eclipse UI Development

Use this skill when working on tasks related to:
- SWT
- JFace
- Eclipse workbench UI
- commands and handlers
- menus and toolbar contributions
- views and editors
- dialogs and wizards
- preference pages
- selections and workbench integration
- UI responsiveness and thread-safety

## Goal

Create UI changes that are consistent with the repository's existing Eclipse UI patterns and remain compatible with the minimum supported Eclipse target-platform and JDK.

## Working Approach

### 1. Follow the repository's existing UI style
Before introducing a UI solution, determine what the repository already uses:
- SWT or JFace conventions
- commands and handlers
- e3 or e4 style workbench integration
- dialogs, wizards, editors, or views
- preference pages and settings storage
- actions vs commands
- existing layout and widget style

Prefer consistency with the current codebase over generic UI patterns.

### 2. Respect the UI thread
For UI-related changes:
- access widgets only from the UI thread
- avoid long-running work on the UI thread
- use background jobs or async patterns for expensive operations
- synchronize back to the UI safely when updating controls

Do not introduce UI freezes for tasks that may block on workspace, network, file system, or computation.

### 3. Use Eclipse workbench patterns
Prefer the workbench-native mechanisms already present in the repository, such as:
- commands and handlers instead of ad hoc listeners where appropriate
- view contributions and editor integration
- structured selection handling
- workbench services
- standard dialogs and message patterns
- preference pages integrated with existing settings flow

### 4. Keep UI state and behavior predictable
UI code should:
- initialize controls consistently
- avoid hidden side effects
- validate user input clearly
- provide actionable messages
- preserve expected workbench behavior
- avoid unnecessary modal interruptions

### 5. Reuse existing Eclipse UI components where possible
Prefer existing widgets, dialogs, label providers, content providers, field editors, viewers, and helper utilities already used in the repository before creating new abstractions.

### 6. Respect internationalization
If the project externalizes strings:
- do not hardcode user-facing UI text
- add or update the relevant messages or properties entries
- keep labels, tooltips, validation messages, and dialog text consistent with the repository's NLS pattern

### 7. Preserve stable UI identifiers and contributions
Unless explicitly requested, do not rename or break:
- command IDs
- handler IDs
- menu contribution IDs
- view IDs
- editor IDs
- preference page IDs
- action definitions
- key binding related identifiers

### 8. Accessibility and usability
When possible:
- use meaningful labels and messages
- avoid unclear abbreviations in visible text
- preserve keyboard navigation
- keep default selections and validation behavior sensible
- avoid unexpected focus changes

### 9. Be careful with lifecycle-sensitive UI code
For views, editors, dialogs, and wizards:
- respect creation and disposal lifecycle
- avoid leaking listeners, images, fonts, colors, or other resources
- dispose custom resources properly
- avoid assuming controls exist before creation or after disposal

## UI Task Checklist

Before considering a UI task complete, check:
- UI access happens on the UI thread
- long-running work does not block the UI
- existing commands/handlers/workbench patterns are respected
- visible strings are externalized if required by the repository
- IDs and contributions remain stable
- resources are disposed properly
- validation and error messages are clear
- solution is compatible with the supported Eclipse and JDK baseline

## Output Expectations

When proposing or implementing Eclipse UI changes, include relevant notes about:
- how the solution fits the repository's existing SWT/JFace/workbench patterns
- how UI thread safety and responsiveness are handled
- whether `plugin.xml`, messages/properties files, or preference metadata also need updates
- any lifecycle or compatibility risks introduced by the change