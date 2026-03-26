# Walkthrough — Step 1: Multi-Module Maven Scaffold

> **Date:** 2026-03-25
> **Step:** 1 of 7 (Slice 1)

## What Changed

### Project structure (before → after)

```
BEFORE (single module):           AFTER (5 modules):
datarunapi/                       datarunapi/
├── pom.xml   (jar)               ├── pom.xml           (pom — parent)
├── src/                          ├── legacy/
│   ├── main/java/...             │   ├── pom.xml
│   ├── main/resources/           │   └── src/ (634 source files)
│   └── test/                     ├── platform-core/
└── ...                           │   ├── pom.xml
                                  │   └── src/ (package-info.java)
                                  ├── platform-capture/
                                  │   ├── pom.xml
                                  │   └── src/ (package-info.java)
                                  ├── platform-flow/
                                  │   ├── pom.xml
                                  │   └── src/ (package-info.java)
                                  ├── app/
                                  │   ├── pom.xml
                                  │   └── src/
                                  │       ├── main/java/.../DataRunApiApp.java
                                  │       ├── main/resources/config/
                                  │       └── test/.../arch/ModuleBoundaryTest.java
                                  └── ...
```

### Key files

| File | Change |
|------|--------|
| `pom.xml` (root) | Converted from `jar` → `pom` parent with 5 modules |
| `legacy/pom.xml` | All existing dependencies |
| `platform-core/pom.xml` | Minimal: JPA + Web + Validation |
| `platform-capture/pom.xml` | Depends on `platform-core` |
| `platform-flow/pom.xml` | Depends on `platform-core` + `platform-capture` |
| `app/pom.xml` | Assembly: all modules + Spring Boot plugin |
| `app/.../DataRunApiApp.java` | Added `scanBasePackages` for both `org.nmcpye.datarun` and `org.nmcpye.platform` |
| `app/.../ModuleBoundaryTest.java` | 3 ArchUnit rules enforcing dependency direction |

## Verification

### ✅ Compilation — PASSED
```
Reactor Summary for Data Run Api 6.3.1:
  Data Run Api ....................................... SUCCESS
  Data Run Legacy .................................... SUCCESS [ 25.933 s]
  Platform Core ...................................... SUCCESS [  0.572 s]
  Platform Capture ................................... SUCCESS [  0.601 s]
  Platform Flow ...................................... SUCCESS [  0.609 s]
  Data Run App ....................................... SUCCESS [  0.758 s]
  BUILD SUCCESS
```

### ✅ Install — PASSED
```
Reactor Summary for Data Run Api 6.3.1:
  Data Run Api ....................................... SUCCESS [ 32.163 s]
  Data Run Legacy .................................... SUCCESS [ 26.610 s]
  Platform Core ...................................... SUCCESS [  0.144 s]
  Platform Capture ................................... SUCCESS [  1.387 s]
  Platform Flow ...................................... SUCCESS [  1.132 s]
  Data Run App ....................................... SUCCESS [ 15.195 s]
  BUILD SUCCESS
```

### ArchUnit test — run with:
```bash
./mvnw test -pl app -Dtest=ModuleBoundaryTest
```
