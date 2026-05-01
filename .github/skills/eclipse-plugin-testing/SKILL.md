---
name: eclipse-plugin-testing
description: Guidance for testing Eclipse plugins with JUnit, PDE, Tycho, SWTBot, runtime tests, and UI stability considerations.
---

# Eclipse Plugin Testing

Use this skill when working on tasks related to:
- JUnit tests for plugin code
- PDE or runtime tests
- Tycho test execution
- SWTBot tests
- UI interaction tests
- plugin integration tests
- test stability for Eclipse-based code

## Goal

Add or modify tests that are reliable, maintainable, and compatible with the repository's minimum supported Eclipse target-platform and JDK.

## Working Approach

### 1. Follow the repository's existing test structure
Before adding tests, determine what the repository already uses:
- plain JUnit unit tests
- plugin/runtime tests
- PDE test setup
- Tycho surefire or failsafe style execution
- SWTBot UI tests
- test fragment or dedicated test bundles
- headless vs UI-enabled test modules

Prefer the existing testing approach over introducing a new one.

### 2. Choose the lightest valid test level
Prefer the simplest test type that can verify the behavior correctly:
- use unit tests for isolated logic
- use plugin/runtime tests when Eclipse runtime integration is required
- use SWTBot or UI tests only when real UI behavior must be verified

Avoid solving purely non-UI logic with expensive UI-level tests.

### 3. Keep tests compatible with target-platform and JDK constraints
Test code must also respect:
- the minimum supported Eclipse target-platform
- the minimum supported JDK
- the repository's actual test runtime setup

Do not use newer test libraries, APIs, or Java features that exceed the current project baseline unless explicitly requested.

### 4. Avoid fragile UI tests
For SWTBot or other UI-driven tests:
- avoid unnecessary sleeps
- prefer explicit waiting conditions
- keep interactions deterministic
- reduce timing sensitivity
- avoid dependence on screen layout or environment-specific focus behavior where possible

### 5. Respect plugin/runtime realities
For Eclipse plugin tests, be careful about:
- workspace state
- active workbench state
- UI thread constraints
- bundle activation timing
- asynchronous jobs
- service availability
- preference persistence
- platform-specific behavior

Tests should isolate assumptions as much as possible.

### 6. Keep test setup clear and minimal
Only add the setup required to verify the behavior under test.  
Avoid broad fixture initialization when a narrow fixture will do.

### 7. Maintain test readability
Tests should make it clear:
- what is being verified
- under what conditions
- what result is expected
- why a runtime or UI-level test is necessary if one is used

### 8. Handle cleanup properly
Tests should clean up:
- workspace resources they create
- preferences they modify
- listeners or services they register
- open UI artifacts when relevant
- temporary files or runtime state

Avoid leaving hidden shared state between test runs.

## Testing Checklist

Before considering a testing task complete, check:
- chosen test level is appropriate
- tests align with the repository's existing framework and structure
- tests are compatible with minimum Eclipse and JDK versions
- UI tests avoid brittle timing assumptions
- runtime assumptions are explicit and controlled
- created resources and state are cleaned up
- test names and assertions clearly describe behavior

## Output Expectations

When proposing or implementing plugin tests, include relevant notes about:
- why the chosen test level is appropriate
- whether the tests require PDE, Tycho runtime, or SWTBot support
- any extra test bundle, manifest, or build updates that may be required
- any known sources of test fragility and how they were minimized