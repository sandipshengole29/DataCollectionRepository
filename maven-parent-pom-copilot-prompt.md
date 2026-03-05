# 🚀 GitHub Copilot Prompt — Maven Parent POM Extraction for Spring Boot 3.x Microservices

> **How to use this file:**
> Open GitHub Copilot Chat in VS Code → paste the prompt block from any step directly into the chat.
> Replace every `[PLACEHOLDER]` with your actual content before sending.

---

## 📋 Overview of the Workflow

```
STEP 1  →  Parse all pom.xml files and list every dependency
STEP 2  →  Compute the true intersection (deps present in ALL services)
STEP 3  →  Generate the parent pom.xml with common dependencies
STEP 4  →  Clean each child pom.xml  ⚠ BEFORE parent injection
STEP 5  →  Inject parent reference into every child pom.xml
STEP 6  →  Final validation report
```

> ⚠️ **Critical Rule:** Execute steps in strict order.
> Never skip ahead. Step 4 MUST be verified complete before Step 5 runs.

---

## 🗂️ Prerequisites — Attach Your Files First

Before running any prompt below, open every `pom.xml` file in VS Code
and attach them using the 📎 **Add Context** button in Copilot Chat,
or paste their contents inline inside the prompt where indicated.

```
Files to attach:
  ├── [service-1-name]/pom.xml
  ├── [service-2-name]/pom.xml
  ├── [service-3-name]/pom.xml
  └── ... (add all services)
```

---

## ▶️ STEP 1 — Parse All Dependencies

> **Paste this into GitHub Copilot Chat**

```
I have multiple Spring Boot 3.x Maven microservices.
The pom.xml files are provided below (or attached as context).

TASK: Read every pom.xml file carefully.
For each service, extract and list EVERY <dependency> entry found
inside the <dependencies> section.

Output format for each service:

  Service: [service-name]
  Spring Boot Parent Version: X.X.X
  ─────────────────────────────────────────────────────
  → groupId:artifactId | version: X.X.X | scope: compile/test/provided/runtime

Rules:
  ✔ Include ALL dependencies — do NOT skip test, runtime, or provided scoped ones
  ✔ If <version> is managed by parent BOM and not explicit, write: version: (managed)
  ✔ If <scope> is absent, write: scope: compile (default)
  ✔ List services in the same order I provided them
  ✔ Do NOT skip any dependency — completeness is critical

Do NOT proceed to intersection analysis yet.
Confirm at the end: "Parsed X services. Total unique dependency entries found: Y"

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
POM — [service-1-name]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[PASTE pom.xml content here]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
POM — [service-2-name]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[PASTE pom.xml content here]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
POM — [service-3-name]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[PASTE pom.xml content here]

(Add more POM blocks as needed — one block per service)
```

---

## ▶️ STEP 2 — Compute the True Intersection

> **Paste this into GitHub Copilot Chat after Step 1 is complete**

```
Using the dependency lists you produced in Step 1, now compute
the TRUE INTERSECTION of dependencies across ALL services.

INTERSECTION RULE:
  A dependency qualifies as COMMON only if its groupId AND artifactId
  BOTH appear in EVERY SINGLE pom.xml provided.
  If it is present in 2 out of 3 services → it does NOT qualify.
  It must exist in ALL services without exception.

VERSION RESOLUTION RULE (when versions differ across services):
  → Use the HIGHEST version number for the parent

SCOPE RESOLUTION RULE (when scopes differ across services):
  → Use the most permissive scope in this order: compile > runtime > provided > test

Output format for each common dependency:

  ┌──────────────────────────────────────────────────────────────┐
  │ COMMON: groupId:artifactId                                   │
  │   [service-1]  → version: X.X  scope: Y                     │
  │   [service-2]  → version: X.X  scope: Y                     │
  │   [service-3]  → version: X.X  scope: Y                     │
  │   ─────────────────────────────────────────────────────────  │
  │   Will use in parent → version: X.X  scope: Y               │
  └──────────────────────────────────────────────────────────────┘

At the end, print:
  "Total common dependencies found: X"
  "Non-common dependencies (stay in children only): Y"

⛔ STOP CONDITION: If zero common dependencies are found, stop here
   and report it. Do NOT proceed to Step 3.

Do NOT create any files yet. Confirm intersection list only.
```

---

## ▶️ STEP 3 — Generate the Parent pom.xml

> **Paste this into GitHub Copilot Chat after Step 2 is confirmed**

```
Using ONLY the common dependencies identified in Step 2,
generate a complete and valid parent pom.xml file.

Use this exact XML structure:

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>microservices-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <name>Microservices Parent POM</name>
    <description>
        Parent POM — manages common dependencies for all microservices.
    </description>

    <!-- Inherit Spring Boot defaults -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>[USE SPRING BOOT VERSION FROM CHILD POMS]</version>
        <relativePath/>
    </parent>

    <!-- List every child service as a module -->
    <modules>
        <module>[service-1-name]</module>
        <module>[service-2-name]</module>
        <module>[service-3-name]</module>
    </modules>

    <!-- Centralise version numbers as properties -->
    <properties>
        <java.version>17</java.version>
        <!-- Add one property per dependency version, e.g.: -->
        <!-- <some.library.version>X.X.X</some.library.version> -->
    </properties>

    <!--
        IMPORTANT: Use <dependencyManagement> — NOT bare <dependencies>
        Children inherit version + scope from here but must
        still declare the dependency (without version) to use it.
    -->
    <dependencyManagement>
        <dependencies>
            <!-- ALL COMMON DEPENDENCIES GO HERE — WITH VERSION TAGS -->
        </dependencies>
    </dependencyManagement>

</project>

STRICT RULES for building this parent pom:
  ✔ ONLY include dependencies from the Step 2 intersection list — nothing extra
  ✔ Use <dependencyManagement> — NEVER bare <dependencies> at parent level
  ✔ Every dependency inside <dependencyManagement> MUST have a <version> tag
  ✔ Use ${property} references in <version> and define them in <properties>
  ✔ Use the highest version when Step 2 versions differed
  ✔ Use the most permissive scope when Step 2 scopes differed
  ✔ Spring Boot 3.x parent must be declared using the version from the child POMs
  ✔ All child service names must appear inside <modules>

After generating the parent pom, print this checklist:
  [ ] Every common dependency from Step 2 is present in <dependencyManagement>
  [ ] <packaging>pom</packaging> is set
  [ ] <dependencyManagement> is used — not bare <dependencies>
  [ ] Spring Boot 3.x parent version is correctly declared
  [ ] <modules> lists all services
  [ ] <properties> contains version entries for all managed dependencies

⛔ DO NOT modify any child pom.xml yet.
   The parent must be finalised and confirmed before touching any child.
```

---

## ▶️ STEP 4 — Clean Every Child pom.xml

> **Paste this into GitHub Copilot Chat after Step 3 parent is confirmed**
> ⚠️ This step MUST be completed and verified BEFORE Step 5

```
The parent pom.xml is now finalised (from Step 3).

TASK: Clean EVERY child pom.xml by removing all dependencies
that are now managed in the parent's <dependencyManagement>.

Process each service ONE AT A TIME using these rules:

  RULE A — REMOVE:
    Delete the complete <dependency>...</dependency> block for any
    dependency whose groupId + artifactId exists in the parent
    <dependencyManagement> list.
    Remove the entire block — do NOT leave a version-less entry behind.

  RULE B — KEEP:
    Retain all dependencies whose groupId + artifactId does NOT appear
    in the parent. Keep them exactly as-is, including version and scope.

  RULE C — DO NOT ADD:
    Do not add version-less <dependency> stubs for removed items.
    Removal means the dependency block is completely gone from the child.

  RULE D — DO NOT INJECT PARENT YET:
    Do NOT add the <parent> block in this step.
    That happens in Step 5 only.

Output the FULL cleaned pom.xml for EACH service.

After each service, print this mandatory verification block:

  ╔═══════════════════════════════════════════════════════════════╗
  ║ Service : [service-name]                                      ║
  ║ Removed (moved to parent) :                                   ║
  ║   → groupId:artifactId                                        ║
  ║   → groupId:artifactId                                        ║
  ║ Still remaining in child :                                    ║
  ║   → groupId:artifactId  version: X.X  scope: Y               ║
  ║ Any common dependency still present in child? ✅ NO / ❌ YES  ║
  ╚═══════════════════════════════════════════════════════════════╝

⛔ GATE CHECK — before printing "Step 4 complete":
   Every service block above MUST show ✅ NO.
   If any service shows ❌ YES, fix that service before continuing.
   Do NOT proceed to Step 5 until all services show ✅ NO.
```

---

## ▶️ STEP 5 — Inject Parent Reference Into Every Child

> **Paste this into GitHub Copilot Chat ONLY after all Step 4 gates show ✅ NO**

```
Step 4 is verified — all common dependencies have been removed from
every child pom.xml.

TASK: Inject the parent pom reference into EVERY child pom.xml.

For each service:

  ACTION A — Remove the existing Spring Boot <parent> block:
    Each child currently has:
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
          ...
      </parent>
    DELETE this block — the child will now inherit Spring Boot
    transitively through the new microservices parent.

  ACTION B — Add the new parent reference:
    Insert the following as the FIRST element inside <project>,
    before <groupId>:

      <parent>
          <groupId>com.example</groupId>
          <artifactId>microservices-parent</artifactId>
          <version>1.0.0</version>
          <relativePath>../pom.xml</relativePath>
      </parent>

  ACTION C — Output the final complete pom.xml:
    Show the entire updated pom.xml for each service containing:
      ✔ The new <parent> block at the top of <project>
      ✔ Only the non-common <dependencies> that were kept in Step 4
      ✔ No duplicate dependencies that exist in the parent
      ✔ No <version> tags on dependencies now managed by the parent

Output one complete final pom.xml per service.
Label each clearly:

  ━━━ FINAL: [service-name]/pom.xml ━━━
  [full xml here]
```

---

## ▶️ STEP 6 — Final Validation Report

> **Paste this into GitHub Copilot Chat after Step 5 is complete**

```
All steps are complete. Generate the final validation report.

SECTION 1 — COMMON DEPENDENCIES (moved to parent):

  ┌──────────────────────────────────┬─────────────────┬───────────────────────────┐
  │ groupId:artifactId               │ Version (parent)│ Removed from services     │
  ├──────────────────────────────────┼─────────────────┼───────────────────────────┤
  │ [groupId]:[artifactId]           │ X.X.X           │ svc-1, svc-2, svc-3       │
  └──────────────────────────────────┴─────────────────┴───────────────────────────┘

SECTION 2 — UNIQUE DEPENDENCIES (kept only in specific services):

  ┌──────────────────────────────────┬──────────┬────────┬──────────────────────────┐
  │ groupId:artifactId               │ Version  │ Scope  │ Only in service          │
  ├──────────────────────────────────┼──────────┼────────┼──────────────────────────┤
  │ [groupId]:[artifactId]           │ X.X.X    │ test   │ [service-name]           │
  └──────────────────────────────────┴──────────┴────────┴──────────────────────────┘

SECTION 3 — FINAL CONFIRMATION CHECKLIST:

  Parent pom.xml
  [ ] Created with ALL common dependencies in <dependencyManagement>
  [ ] <packaging>pom</packaging> is set
  [ ] Spring Boot 3.x inherited via spring-boot-starter-parent
  [ ] All child services listed in <modules>

  Per-service child pom.xml
  [ ] [service-1] — 0 common deps remain — parent reference injected ✅
  [ ] [service-2] — 0 common deps remain — parent reference injected ✅
  [ ] [service-3] — 0 common deps remain — parent reference injected ✅

  Overall
  [ ] No version conflicts exist across the parent-child POM chain
  [ ] Spring Boot 3.x dependency resolution is intact for all services
  [ ] Each child can be built independently with: mvn clean install
```

---

## 🗂️ Final Directory Structure (Expected Result)

```
your-project/
│
├── pom.xml                          ← NEW parent pom (packaging: pom)
│
├── [service-1-name]/
│   └── pom.xml                      ← cleaned + parent injected
│
├── [service-2-name]/
│   └── pom.xml                      ← cleaned + parent injected
│
└── [service-3-name]/
    └── pom.xml                      ← cleaned + parent injected
```

---

## ⚡ Quick Reference — Step Execution Checklist

| Step | Action | Gate Before Next Step |
|------|--------|-----------------------|
| **1** | Parse all pom.xml files | All services listed completely |
| **2** | Find intersection (deps in ALL services) | Count confirmed |
| **3** | Generate parent pom.xml | All checklist items ✅ |
| **4** | Clean every child pom.xml | Every service shows ✅ NO common deps |
| **5** | Inject parent into every child | Full final pom.xml shown per service |
| **6** | Final validation report | All checklist items ✅ |

---

## 💡 Tips for GitHub Copilot

- Use **Copilot Chat** (`Ctrl+Shift+I` / `Cmd+Shift+I`) not inline suggestions
- Attach pom.xml files via the **📎 Add Context** button for cleaner prompts
- If Copilot truncates output, follow up with: `"Continue from where you stopped"`
- If a step result looks incomplete, say: `"Repeat Step X output in full — do not truncate"`
- Run each step as a **separate Copilot Chat message** to avoid context overflow
- After Step 5, validate each pom with: `mvn validate -f [service]/pom.xml`

---

*Generated for Spring Boot 3.x multi-module Maven microservices.*
