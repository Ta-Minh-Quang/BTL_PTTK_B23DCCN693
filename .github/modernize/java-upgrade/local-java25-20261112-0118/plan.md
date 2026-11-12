# Upgrade Plan: restman-backend-B23DCCN693 (local-java25-20261112-0118)

- **Generated**: 2026-11-12 01:18 +07:00
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

> Local execution record: version control is unavailable in this workspace; upgrade telemetry/plan tools were not exposed in this session.

## Available Tools

**JDKs**
- JDK 17: not available (baseline will be skipped)
- JDK 21.0.12: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot` (current runtime)
- JDK 25.0.5: **<TO_BE_INSTALLED>** (required for final validation)

**Build Tools**
- Maven 3.9.16: `C:\Users\ADMIN\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd`

## Guidelines

- Preserve existing application behavior and limit changes to Java runtime/compiler configuration and its documentation.

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: N/A (not a git repository)
- Run tests before and after the upgrade: true

## Upgrade Goals

- Java 25 LTS

## Technology Stack

| Technology/Dependency | Current | Min Compatible Version | Why Incompatible |
| --------------------- | ------- | ---------------------- | ---------------- |
| Java release target | 17 | 25 | User requested Java 25 |
| maven-compiler-plugin | 3.13.0 | 3.13.0 | Supports `maven.compiler.release`; javac from the selected JDK determines supported Java release |
| Maven | 3.9.16 (cached) | 3.9+ | Compatible; no upgrade needed |
| MySQL Connector/J | 8.4.0 | 8.4.0 | No Java 25-specific change required |

## Derived Upgrades

- Update the README's JDK requirement to match the new Java 25 runtime target.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|-----------|---------|--------|--------|--------|
No dependency/plugin version changes are required; the compiler plugin delegates release support to JDK `javac`.

### Source Code Changes

No source changes identified; the project uses standard Java/Swing and JDBC APIs.

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|-----------------|--------|
| pom.xml | `maven.compiler.release` | 17 | Set to 25 | Compile project to Java 25 API and bytecode level |

### CI/CD Changes

No CI/CD files or JDK pins found.

### Documentation Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|-----------------|--------|
| README.md | Requirements section | JDK 17 or newer | State JDK 25 LTS or newer | Keep documented runtime requirement aligned with the target |

### Risks & Warnings

- JDK 25 is not currently installed; install a Java 25 distribution before final verification. If installation fails, compilation cannot be validated on the target JDK.
- JDK 17 is absent, so a baseline run on the original configured Java release will be skipped; Java 21 is available but is not the base JDK.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Target-JDK validation requires Java 25; cached Maven 3.9.16 is already available.
  - **Changes to Make**: Install JDK 25; use cached Maven 3.9.16.
  - **Verification**: `java -version` using installed JDK 25; verify Maven executable exists.
- Step 2: Setup Baseline
  - **Rationale**: Record pre-upgrade behavior when the project's base JDK is available.
  - **Changes to Make**: None; skip because JDK 17 is unavailable.
  - **Verification**: Skipped (base JDK unavailable).
- Step 3: Upgrade Java Runtime Configuration
  - **Rationale**: Set the compiler release to the requested target and update matching documentation.
  - **Changes to Make**: Apply Configuration Changes and the related README requirement change.
  - **Verification**: `mvn clean test-compile -q` with JDK 25.
- Step 4: CVE Validation
  - **Rationale**: Check direct dependency versions for known vulnerabilities without broad dependency changes.
  - **Changes to Make**: Scan direct dependencies; change versions only if a vulnerability is reported with a compatible fix.
  - **Verification**: Dependency listing and available Java CVE scanner; compile after any fixes.
- Step 5: Final Validation
  - **Rationale**: Verify the exact target runtime/compiler configuration and execute available tests.
  - **Changes to Make**: Fix any build/test regressions.
  - **Verification**: `mvn clean test -q` with JDK 25; expected build success.
