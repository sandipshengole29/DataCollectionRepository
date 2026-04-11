# 🚀 GitHub Copilot Prompt — Maven Parent POM Extraction for Spring Boot 3.x Microservices

---

## 📋 How to Use This File

Each step is structured as a **TASK** with two parts:

```
┌─────────────────────────────────────────────────────────────┐
│  PART A — TASK PROMPT                                        │
│  Paste into Copilot Chat. Fill all 📥 sections first.        │
├─────────────────────────────────────────────────────────────┤
│  PART B — TASK SUMMARY TEMPLATE                              │
│  Fill in AFTER Copilot responds. Save it. Paste it into      │
│  the next task prompt at the 📥 RESUME section.             │
└─────────────────────────────────────────────────────────────┘
```

### ♻️ Resume Rule — if Copilot fails or session is lost:

> Find the **last task whose SUMMARY you completed**.
> Go to the **next task's prompt**.
> Paste that summary into its `📥 RESUME FROM LAST COMPLETED TASK SUMMARY` section.
> Send the prompt. Copilot will resume from that point with full context.

### ⚠️ Critical Rules
- Execute tasks in strict order — never skip ahead
- Never send a prompt with an unfilled `📥` section — Copilot will hallucinate instead of using your real data
- If Copilot truncates output mid-response, say: `"Continue from where you stopped — do not restart"`

---

## 🗂️ Task Overview

| Task | Action | Produces Summary Of |
|------|--------|---------------------|
| **1** | Parse all pom.xml files | Full dependency list per service |
| **2** | Intersection + TIER classification | TIER-1 / TIER-2 / TIER-3 tables |
| **3** | Generate parent pom.xml | Complete parent pom.xml XML |
| **4** | Clean every child pom.xml | Cleaned child pom.xml per service |
| **5** | Inject parent reference into children | Final child pom.xml per service |
| **6** | Final validation report | — terminal task, no summary needed |

---

## 🗂️ Prerequisites — Attach Your Files First

Before running Task 1, open every `pom.xml` in VS Code and either:
- Attach them via the **📎 Add Context** button in Copilot Chat, or
- Paste their contents inline into the Task 1 prompt at the marked sections

```
Files needed:
  ├── [service-1-name]/pom.xml
  ├── [service-2-name]/pom.xml
  ├── [service-3-name]/pom.xml
  └── ... (one block per service)
```

---

---

# ▶️ TASK 1 — Parse All Dependencies

---

## PART A — TASK 1 PROMPT

> Paste into **GitHub Copilot Chat**. No prior summary needed — this is the first task.

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

## PART B — TASK 1 SUMMARY TEMPLATE

> ✏️ Fill this in AFTER Copilot responds to Task 1.
> Save this block. You will paste it into the Task 2 prompt.

```
╔══════════════════════════════════════════════════════════════════╗
║  TASK 1 SUMMARY — Dependency Parse Complete                      ║
╠══════════════════════════════════════════════════════════════════╣
║  Services parsed: [X]                                            ║
║  Spring Boot version: [X.X.X]                                   ║
╠══════════════════════════════════════════════════════════════════╣
║  SERVICE DEPENDENCY LISTS                                        ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  Service: [service-1-name]                                       ║
║  → [groupId:artifactId] | version: X.X | scope: Y               ║
║  → [groupId:artifactId] | version: X.X | scope: Y               ║
║  (list all deps for this service)                                ║
║                                                                  ║
║  Service: [service-2-name]                                       ║
║  → [groupId:artifactId] | version: X.X | scope: Y               ║
║  → [groupId:artifactId] | version: X.X | scope: Y               ║
║  (list all deps for this service)                                ║
║                                                                  ║
║  Service: [service-3-name]                                       ║
║  → [groupId:artifactId] | version: X.X | scope: Y               ║
║  → [groupId:artifactId] | version: X.X | scope: Y               ║
║  (list all deps for this service)                                ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

---

---

# ▶️ TASK 2 — Compute Intersection and Classify by Tier

---

## PART A — TASK 2 PROMPT

> Fill in the `📥 RESUME` section below before sending to Copilot Chat.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 RESUME FROM LAST COMPLETED TASK SUMMARY
    Paste your completed TASK 1 SUMMARY block here:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 1 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Using the dependency lists in the summary above, compute the TRUE
INTERSECTION of dependencies across ALL services, then classify
each common dependency into one of three tiers.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
INTERSECTION RULE:
  A dependency qualifies as COMMON only if its groupId AND artifactId
  BOTH appear in EVERY SINGLE pom.xml provided.
  If it is present in 2 out of 3 services → it does NOT qualify.
  It must exist in ALL services without exception.

VERSION RESOLUTION RULE (when versions differ across services):
  → Use the HIGHEST version number for the parent

SCOPE RESOLUTION RULE (when scopes differ across services):
  → Use the most permissive scope: compile > runtime > provided > test

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
TIER CLASSIFICATION RULE:

  TIER-1 → bare <dependencies> in parent (force-injected into ALL children)
    Criteria — qualifies ONLY IF ALL of the following are true:
      ✔ Present in every service (passes intersection rule)
      ✔ Universal cross-cutting concern with NO domain specificity
      ✔ Every child module will ALWAYS need it — no exceptions
      ✔ Belongs to one of these categories:
            - Test framework starters (e.g. spring-boot-starter-test, scope: test)
            - Compile-time generators (e.g. lombok, scope: provided)
            - Universal observability agents used in every service
      ⛔ STRICT LIMIT: TIER-1 must be a very small, honest set.
         When in doubt → assign TIER-2 instead.
         Domain-specific starters (JPA, Kafka, Security, Web) are NEVER TIER-1.

  TIER-2 → <dependencyManagement> in parent (version-governed, child opts in)
    Criteria:
      ✔ Passes intersection rule BUT is domain-specific or feature-specific
      ✔ Children must still declare it (without <version>) to use it
      Examples: spring-boot-starter-data-jpa, spring-boot-starter-web,
                spring-kafka, springdoc-openapi, flyway-core, mapstruct

  TIER-3 → stays in child pom.xml only (not in parent at all)
    Criteria:
      ✔ Does NOT pass intersection rule — not present in all services

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Output format:

  ┌──────────────────────────────────────────────────────────────┐
  │ TIER-1 (bare <dependencies> in parent):                      │
  │   groupId:artifactId  → version: X.X  scope: Y              │
  │   Reason: [why this is genuinely universal]                  │
  ├──────────────────────────────────────────────────────────────┤
  │ TIER-2 (<dependencyManagement> in parent):                   │
  │   groupId:artifactId  → resolved version: X.X  scope: Y     │
  │   [service-1] → version: X.X  scope: Y                      │
  │   [service-2] → version: X.X  scope: Y                      │
  │   [service-3] → version: X.X  scope: Y                      │
  ├──────────────────────────────────────────────────────────────┤
  │ TIER-3 (child only — did not pass intersection):             │
  │   groupId:artifactId  → present in: [service-name] only     │
  └──────────────────────────────────────────────────────────────┘

At the end, print:
  "TIER-1 (bare deps in parent): X  ← must be a small number"
  "TIER-2 (dependencyManagement in parent): Y"
  "TIER-3 (child only): Z"

⛔ STOP CONDITION: If zero common dependencies are found, stop and report.
   Do NOT proceed to Task 3.

⚠️  TIER-1 SANITY CHECK: If TIER-1 count exceeds 3–4, re-evaluate.
    Move borderline deps to TIER-2.

Do NOT create any files yet. Confirm tier classification only.
```

---

## PART B — TASK 2 SUMMARY TEMPLATE

> ✏️ Fill this in AFTER Copilot responds to Task 2.
> Save this block. You will paste it into Task 3 AND Task 4 AND Task 6 prompts.

```
╔══════════════════════════════════════════════════════════════════╗
║  TASK 2 SUMMARY — Tier Classification Complete                   ║
╠══════════════════════════════════════════════════════════════════╣
║  Spring Boot version: [X.X.X]                                   ║
║  TIER-1 count: [X]   TIER-2 count: [Y]   TIER-3 count: [Z]     ║
╠══════════════════════════════════════════════════════════════════╣
║  TIER-1 — bare <dependencies> in parent (auto-injected)          ║
╠══════════════════════════════════════════════════════════════════╣
║  groupId:artifactId | scope | reason it is universal            ║
║  [fill one line per TIER-1 dep]                                  ║
╠══════════════════════════════════════════════════════════════════╣
║  TIER-2 — <dependencyManagement> in parent (child opts in)       ║
╠══════════════════════════════════════════════════════════════════╣
║  groupId:artifactId | resolved-version | resolved-scope         ║
║  [fill one line per TIER-2 dep]                                  ║
╠══════════════════════════════════════════════════════════════════╣
║  TIER-3 — child only (no change)                                 ║
╠══════════════════════════════════════════════════════════════════╣
║  groupId:artifactId | version | scope | only-in-service         ║
║  [fill one line per TIER-3 dep]                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

---

---

# ▶️ TASK 3 — Generate the Parent pom.xml

---

## PART A — TASK 3 PROMPT

> Fill in the `📥 RESUME` section below before sending to Copilot Chat.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 RESUME FROM LAST COMPLETED TASK SUMMARY
    Paste your completed TASK 2 SUMMARY block here:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 2 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Using the TIER classification in the summary above, generate a
complete and valid parent pom.xml file using this exact structure:

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
        <version>[USE SPRING BOOT VERSION FROM TASK 2 SUMMARY]</version>
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
        <!-- Add one property per TIER-2 dependency version -->
    </properties>

    <!--
        TIER-1: bare <dependencies> — force-injected into ALL children.
        ⛔ STRICT DISCIPLINE: Keep this block very small and honest.
           Only dependencies from the TIER-1 list in the summary above.
           When in doubt — move to <dependencyManagement> instead.
    -->
    <dependencies>
        <!-- TIER-1 UNIVERSAL DEPENDENCIES ONLY — from TASK 2 SUMMARY TIER-1 list -->
    </dependencies>

    <!--
        TIER-2: <dependencyManagement> — version-governed.
        Children must still declare each one (without <version>) to opt in.
    -->
    <dependencyManagement>
        <dependencies>
            <!-- TIER-2 DEPENDENCIES — from TASK 2 SUMMARY TIER-2 list — WITH VERSION TAGS -->
        </dependencies>
    </dependencyManagement>

</project>

STRICT RULES:
  ✔ TIER-1 deps go in bare <dependencies> — auto-inherited by all children
  ✔ TIER-2 deps go in <dependencyManagement> — children must still declare them
  ✔ Every <dependencyManagement> entry MUST have a <version> tag
  ✔ Bare <dependencies> TIER-1 entries do NOT need <version> if Spring Boot BOM manages them
  ✔ Use ${property} references for versions — define them in <properties>
  ✔ Use highest version and most permissive scope from TASK 2 SUMMARY
  ✔ TIER-1 bare <dependencies> MUST remain very small (2–3 entries max)
  ✔ All service names must appear in <modules>
  ✔ Do NOT include TIER-3 deps — those stay in individual children only

After generating, print this checklist:
  [ ] TIER-1 deps are in bare <dependencies> — auto-inherited by all children
  [ ] TIER-1 count is small and each entry is justified
  [ ] TIER-2 deps are in <dependencyManagement> with <version> tags
  [ ] <packaging>pom</packaging> is set
  [ ] Spring Boot version matches TASK 2 SUMMARY
  [ ] <modules> lists all services
  [ ] <properties> has version entries for all TIER-2 deps

⛔ DO NOT modify any child pom.xml yet.
```

---

## PART B — TASK 3 SUMMARY TEMPLATE

> ✏️ Fill this in AFTER Copilot responds to Task 3.
> Save this block. You will paste it into Task 4 AND Task 6 prompts.

```
╔══════════════════════════════════════════════════════════════════╗
║  TASK 3 SUMMARY — Parent pom.xml Generated                       ║
╠══════════════════════════════════════════════════════════════════╣
║  Checklist status: all items ✅ confirmed                        ║
║  TIER-1 count in bare <dependencies>: [X]                       ║
║  TIER-2 count in <dependencyManagement>: [Y]                    ║
╠══════════════════════════════════════════════════════════════════╣
║  GENERATED parent pom.xml — paste full XML below:               ║
╠══════════════════════════════════════════════════════════════════╣

[PASTE FULL PARENT pom.xml XML HERE]

╚══════════════════════════════════════════════════════════════════╝
```

---

---

# ▶️ TASK 4 — Clean Every Child pom.xml

---

## PART A — TASK 4 PROMPT

> Fill in BOTH `📥 RESUME` sections below before sending to Copilot Chat.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 RESUME FROM LAST COMPLETED TASK SUMMARY
    Paste your completed TASK 2 SUMMARY block here
    (needed for TIER classification rules):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 2 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 PRIOR TASK CONTEXT
    Paste your completed TASK 3 SUMMARY block here
    (needed for the finalised parent pom.xml):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 3 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 CHILD pom.xml FILES TO CLEAN
    Paste each child pom.xml file below — one block per service:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

━━━ CHILD POM — [service-1-name] ━━━
[PASTE pom.xml content here]

━━━ CHILD POM — [service-2-name] ━━━
[PASTE pom.xml content here]

━━━ CHILD POM — [service-3-name] ━━━
[PASTE pom.xml content here]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Using the TIER classification from TASK 2 SUMMARY and the parent
pom.xml from TASK 3 SUMMARY, clean EVERY child pom.xml above.

Process each service ONE AT A TIME using these rules:

  RULE A — REMOVE ENTIRELY (TIER-1 deps):
    Delete the complete <dependency>...</dependency> block for any
    dependency listed in TIER-1 of TASK 2 SUMMARY.
    These are force-injected by the parent's bare <dependencies>.
    The child must NOT declare them at all — not even without a version.

  RULE A2 — REMOVE VERSION TAG ONLY (TIER-2 deps):
    For dependencies listed in TIER-2 of TASK 2 SUMMARY:
      → Keep the <dependency> block
      → Remove ONLY the <version> tag
      → Keep <groupId>, <artifactId>, and <scope> exactly as-is
    Child still opts in — version is governed by parent <dependencyManagement>.

  RULE B — KEEP UNCHANGED (TIER-3 deps):
    Retain dependencies listed in TIER-3 of TASK 2 SUMMARY exactly as-is,
    including <version> and <scope>. No changes.

  RULE C — DO NOT ADD:
    Do not add any dependency blocks not already present.

  RULE D — DO NOT INJECT PARENT YET:
    Do NOT add the <parent> block. That happens in Task 5.

Output the FULL cleaned pom.xml for EACH service.

After each service, print this mandatory verification block:

  ╔═══════════════════════════════════════════════════════════════════╗
  ║ Service : [service-name]                                          ║
  ║ TIER-1 removed entirely:                                         ║
  ║   → groupId:artifactId                                           ║
  ║ TIER-2 version tag stripped (block kept, no <version>):          ║
  ║   → groupId:artifactId                                           ║
  ║ TIER-3 kept unchanged:                                           ║
  ║   → groupId:artifactId  version: X.X  scope: Y                  ║
  ║ Any TIER-1 dep still fully declared in child?  ✅ NO / ❌ YES   ║
  ║ Any TIER-2 dep still carrying a <version> tag? ✅ NO / ❌ YES   ║
  ╚═══════════════════════════════════════════════════════════════════╝

⛔ GATE CHECK — before printing "Task 4 complete":
   Every service block MUST show ✅ NO on BOTH gate lines.
   If any service shows ❌ YES, fix it before continuing.
   Do NOT proceed to Task 5 until all services show ✅ NO.
```

---

## PART B — TASK 4 SUMMARY TEMPLATE

> ✏️ Fill this in AFTER Copilot responds to Task 4 and all gates show ✅ NO.
> Save this block. You will paste it into the Task 5 prompt.

```
╔══════════════════════════════════════════════════════════════════╗
║  TASK 4 SUMMARY — Child pom.xml Files Cleaned                    ║
╠══════════════════════════════════════════════════════════════════╣
║  All TIER-1 removed entirely?         ✅ YES (confirmed)         ║
║  All TIER-2 version tags stripped?    ✅ YES (confirmed)         ║
║  All gate checks passed?              ✅ YES (confirmed)         ║
╠══════════════════════════════════════════════════════════════════╣
║  CLEANED CHILD pom.xml FILES — paste each full XML below:        ║
╠══════════════════════════════════════════════════════════════════╣

━━━ CLEANED: [service-1-name]/pom.xml ━━━
[PASTE FULL CLEANED pom.xml XML HERE]

━━━ CLEANED: [service-2-name]/pom.xml ━━━
[PASTE FULL CLEANED pom.xml XML HERE]

━━━ CLEANED: [service-3-name]/pom.xml ━━━
[PASTE FULL CLEANED pom.xml XML HERE]

╚══════════════════════════════════════════════════════════════════╝
```

---

---

# ▶️ TASK 5 — Inject Parent Reference Into Every Child

---

## PART A — TASK 5 PROMPT

> Fill in the `📥 RESUME` section below before sending to Copilot Chat.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 RESUME FROM LAST COMPLETED TASK SUMMARY
    Paste your completed TASK 4 SUMMARY block here
    (contains all cleaned child pom.xml files):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 4 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Task 4 is verified — all child pom.xml files in the summary above
have been cleaned. All gate checks passed.

TASK: Inject the parent pom reference into EVERY child pom.xml
from the TASK 4 SUMMARY above.

For each service:

  ACTION A — Remove the existing Spring Boot <parent> block:
    Each child currently has:
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
          ...
      </parent>
    DELETE this block entirely — the child will now inherit Spring Boot
    transitively through the new microservices parent.

  ACTION B — Insert the new parent reference:
    Add the following as the FIRST element inside <project>,
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
      ✔ TIER-2 deps declared without <version> (governed by parent)
      ✔ TIER-3 deps kept unchanged with their full <version> and <scope>
      ✔ No TIER-1 deps declared (they are auto-injected by parent)

Output one complete final pom.xml per service. Label each clearly:

  ━━━ FINAL: [service-name]/pom.xml ━━━
  [full xml here]
```

---

## PART B — TASK 5 SUMMARY TEMPLATE

> ✏️ Fill this in AFTER Copilot responds to Task 5.
> Save this block. You will paste it into the Task 6 prompt.

```
╔══════════════════════════════════════════════════════════════════╗
║  TASK 5 SUMMARY — Parent Reference Injected                      ║
╠══════════════════════════════════════════════════════════════════╣
║  Parent block injected into all children? ✅ YES (confirmed)     ║
║  Spring Boot parent block removed from all children? ✅ YES      ║
╠══════════════════════════════════════════════════════════════════╣
║  FINAL CHILD pom.xml FILES — paste each full XML below:          ║
╠══════════════════════════════════════════════════════════════════╣

━━━ FINAL: [service-1-name]/pom.xml ━━━
[PASTE FULL FINAL pom.xml XML HERE]

━━━ FINAL: [service-2-name]/pom.xml ━━━
[PASTE FULL FINAL pom.xml XML HERE]

━━━ FINAL: [service-3-name]/pom.xml ━━━
[PASTE FULL FINAL pom.xml XML HERE]

╚══════════════════════════════════════════════════════════════════╝
```

---

---

# ▶️ TASK 6 — Final Validation Report

---

## PART A — TASK 6 PROMPT

> Fill in ALL THREE `📥` sections below before sending to Copilot Chat.
> This is the terminal task — no summary template needed after this.

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 RESUME FROM LAST COMPLETED TASK SUMMARY
    Paste your completed TASK 2 SUMMARY (tier classification):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 2 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 PRIOR TASK CONTEXT
    Paste your completed TASK 3 SUMMARY (parent pom.xml):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 3 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 PRIOR TASK CONTEXT
    Paste your completed TASK 5 SUMMARY (final child pom.xml files):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[PASTE TASK 5 SUMMARY HERE]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

All tasks are complete. Using ONLY the context provided in the
summaries above, generate the final validation report.

SECTION 1 — TIER-1 DEPENDENCIES (bare <dependencies> — auto-inherited by all children):

  ┌──────────────────────────────────┬──────────┬───────────────────────────────────┐
  │ groupId:artifactId               │ Scope    │ Reason it is genuinely universal  │
  ├──────────────────────────────────┼──────────┼───────────────────────────────────┤
  │ [from TASK 2 SUMMARY TIER-1]     │          │                                   │
  └──────────────────────────────────┴──────────┴───────────────────────────────────┘
  ⚠️  Count: X  (flag if > 3–4 — re-evaluate tier classification)

SECTION 2 — TIER-2 DEPENDENCIES (<dependencyManagement> — children opt in without version):

  ┌──────────────────────────────────┬──────────────────┬───────────────────────────┐
  │ groupId:artifactId               │ Version (parent) │ Present in services       │
  ├──────────────────────────────────┼──────────────────┼───────────────────────────┤
  │ [from TASK 2 SUMMARY TIER-2]     │                  │ svc-1, svc-2, svc-3       │
  └──────────────────────────────────┴──────────────────┴───────────────────────────┘

SECTION 3 — TIER-3 DEPENDENCIES (child-only — unchanged):

  ┌──────────────────────────────────┬──────────┬────────┬──────────────────────────┐
  │ groupId:artifactId               │ Version  │ Scope  │ Only in service          │
  ├──────────────────────────────────┼──────────┼────────┼──────────────────────────┤
  │ [from TASK 2 SUMMARY TIER-3]     │          │        │                          │
  └──────────────────────────────────┴──────────┴────────┴──────────────────────────┘

SECTION 4 — FINAL CONFIRMATION CHECKLIST:

  Parent pom.xml
  [ ] TIER-1 deps in bare <dependencies> — small and justified (≤ 3–4 entries)
  [ ] TIER-2 deps in <dependencyManagement> with explicit <version> tags
  [ ] <packaging>pom</packaging> is set
  [ ] Spring Boot 3.x inherited via spring-boot-starter-parent
  [ ] All child services listed in <modules>

  Per-service child pom.xml
  [ ] [service-1] — 0 TIER-1 deps declared — 0 TIER-2 deps carry <version> — parent injected ✅
  [ ] [service-2] — 0 TIER-1 deps declared — 0 TIER-2 deps carry <version> — parent injected ✅
  [ ] [service-3] — 0 TIER-1 deps declared — 0 TIER-2 deps carry <version> — parent injected ✅

  Overall
  [ ] No version conflicts exist across the parent-child POM chain
  [ ] Spring Boot 3.x dependency resolution is intact for all services
  [ ] Each child can be built independently with: mvn clean install
```

---

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

## ♻️ Resume Reference — Which Summaries Each Task Needs

| If you need to resume at... | Paste these summaries into the prompt |
|-----------------------------|---------------------------------------|
| **Task 2** | Task 1 Summary |
| **Task 3** | Task 2 Summary |
| **Task 4** | Task 2 Summary + Task 3 Summary + raw child pom.xml files |
| **Task 5** | Task 4 Summary |
| **Task 6** | Task 2 Summary + Task 3 Summary + Task 5 Summary |

---

## 💡 Tips for GitHub Copilot

- Use **Copilot Chat** (`Ctrl+Shift+I` / `Cmd+Shift+I`) — not inline suggestions
- **Summaries are your safety net** — always fill them in before moving to the next task
- **Never send a prompt with an unfilled `📥` section** — Copilot will hallucinate data instead of using your real content
- If Copilot truncates output mid-response: `"Continue from where you stopped — do not restart from the beginning"`
- If a task result looks wrong: `"Repeat Task X output in full — do not truncate"`
- After Task 5, validate locally with: `mvn validate -f [service]/pom.xml`
- If Copilot misclassifies a dep in Task 2, correct it before proceeding:
  `"Move [groupId:artifactId] from TIER-1 to TIER-2 — it is domain-specific, not universal"`

---

*Generated for Spring Boot 3.x multi-module Maven microservices.*
