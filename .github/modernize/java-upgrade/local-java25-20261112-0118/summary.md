# Java Upgrade Summary: restman-backend-B23DCCN693

## Result

Configured the project to compile for Java 25 LTS. The final clean Maven test lifecycle succeeded using Temurin JDK 25.0.5+8 and Maven 3.9.16.

## Files Changed

- `pom.xml` — `maven.compiler.release` changed from `17` to `25`; `maven-compiler-plugin` remains at `3.13.0`.
- `README.md` — documented minimum runtime updated from JDK 17+ to JDK 25 LTS+.
- `.github/modernize/java-upgrade/local-java25-20261112-0118/plan.md` — upgrade plan.
- `.github/modernize/java-upgrade/local-java25-20261112-0118/progress.md` — execution record.
- `.github/modernize/java-upgrade/local-java25-20261112-0118/summary.md` — this summary.

## Validation

- `mvn clean test-compile -q` — succeeded on Java 25.
- `mvn clean test -q` — succeeded on Java 25 after final configuration review.
- `javap -verbose` reported classfile major version `69` (Java 25).
- No test sources/reports were present, so the test lifecycle passed with no tests executed.
- Direct dependency check found `com.mysql:mysql-connector-j:8.4.0`; OSV returned no advisories for that coordinate/version.

## Notes

- Baseline was skipped because JDK 17 was not installed; JDK 21 was present before the upgrade.
- This workspace is not a git repository, so changes were not committed.
- No source code or application behavior changes were made.
