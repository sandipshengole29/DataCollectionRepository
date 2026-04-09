# Spring Boot Upgrade Prompt: 3.1.0 → 3.5.13 (GitHub Copilot Chat Edition)
> **Target Spring Boot Version :** 3.5.13
> **Scope                      :** Production-grade microservice with Spring Cloud
>                                  (Eureka, Config, Gateway, OpenFeign, Circuit Breaker)
> **Strategy                   :** Phased, hop-by-hop — never jump directly 3.1.0 → 3.5.13
> **Build Tool                 :** Maven

---

## ⚡ HOW TO USE THIS FILE IN GITHUB COPILOT CHAT

```
RULE 1 — Paste ONE phase at a time.
         Copilot Chat has a context window limit.
         Copy only the phase block you are currently working on.

RULE 2 — Every phase block starts with #file: references.
         These tell Copilot to read your actual project files before acting.
         Do not remove them.

RULE 3 — Audit prompts (Phase 0) are prefixed with @workspace.
         This instructs Copilot to scan the entire codebase, not just the open file.

RULE 4 — Use slash commands for targeted actions:
         /fix   → Fix a compilation error in the currently open file
         /explain → Understand what a deprecated API was replaced with
         /tests → Generate tests for a class you just migrated

RULE 5 — After each phase, run: ./mvnw clean verify
         Do NOT proceed to the next phase until the build is 100% green.
```

---

## SPRING CLOUD RELEASE TRAIN COMPATIBILITY MAP
> ⚠️ CRITICAL — Mismatching Spring Boot and Spring Cloud versions causes
> `NoSuchMethodError`, `ClassNotFoundException`, and startup veto failures.
> Upgrade Spring Cloud BOM in LOCK-STEP with Spring Boot at every phase.

| Spring Boot Version | Spring Cloud Train | Codename    | Patch to Use |
|---------------------|--------------------|-------------|--------------|
| 3.1.x               | 2022.0.x           | Kilburn     | 2022.0.x     |
| 3.2.x / 3.3.x      | 2023.0.x           | Leyton      | 2023.0.6     |
| 3.4.x               | 2024.0.x           | Moorgate    | 2024.0.3     |
| 3.5.x               | 2025.0.x           | Northfields | 2025.0.1     |

---

## UPGRADE PHASE OVERVIEW

```
Phase 0 : Pre-Upgrade Audit    — @workspace scans, no version changes
Phase 1 : 3.1.0  → 3.2.x      — Spring Cloud 2022.0.x → 2023.0.x
Phase 2 : 3.2.x  → 3.3.x      — Spring Cloud stays on 2023.0.x
Phase 3 : 3.3.x  → 3.4.x      — Spring Cloud 2023.0.x → 2024.0.x  ⚠️ HIGH RISK
Phase 4 : 3.4.x  → 3.5.13     — Spring Cloud 2024.0.x → 2025.0.x  ⚠️ HIGH RISK
Phase 5 : Post-Upgrade Tasks   — Cleanup, Java 21, final verification
```

---
---

# ══════════════════════════════════════════════════
# PHASE 0 — PRE-UPGRADE AUDIT
# Paste this entire block into GitHub Copilot Chat
# ══════════════════════════════════════════════════

```
@workspace I am upgrading this Spring Boot microservice from version 3.1.0 to 3.5.13.
Before I change any version in pom.xml, perform the following audits across the
entire codebase and report your findings for each item.

Read these files for full project context:
#file:pom.xml
#file:src/main/resources/application.yml
#file:src/main/resources/application-local.yml
#file:src/main/resources/application-docker.yml
#file:src/main/resources/application-prod.yml

════════════════════════════════════════
AUDIT 1 — SPRING CLOUD COMPONENT SCAN
════════════════════════════════════════
Scan #file:pom.xml and list every Spring Cloud dependency present in this project.
Map each dependency to the component it represents from this list:
- spring-cloud-starter-netflix-eureka-client  → Service Discovery Client
- spring-cloud-starter-netflix-eureka-server  → Eureka Registry Server
- spring-cloud-starter-config                 → Config Server Client
- spring-cloud-config-server                  → Config Server
- spring-cloud-starter-gateway                → API Gateway
- spring-cloud-starter-openfeign              → Declarative HTTP Client
- spring-cloud-starter-circuitbreaker-resilience4j → Circuit Breaker
- micrometer-tracing-bridge-brave/otel        → Distributed Tracing
- spring-cloud-bus                            → Distributed Event Bus
- spring-cloud-stream                         → Messaging Abstraction
- spring-cloud-kubernetes                     → Kubernetes Integration

════════════════════════════════════════
AUDIT 2 — DEPRECATED API SCAN
════════════════════════════════════════
Search the entire src/main/java and src/test/java tree for:
- Any class, method, or field annotated with @Deprecated from Spring packages
- Any usage of Spring Boot, Spring Security, Spring Data, or Spring Cloud APIs
  that are known to have been deprecated between versions 3.1.x and 3.2.x
Report each finding as: FILE PATH | CLASS | METHOD/FIELD | DEPRECATED SINCE | REPLACEMENT

════════════════════════════════════════
AUDIT 3 — PROPERTY FILE VIOLATIONS
════════════════════════════════════════
Scan all property files listed above and find:

A) Any .enabled property whose value is NOT exactly 'true' or 'false'
   (values like 'yes', '1', 'on', 'TRUE' will cause BindException in Spring Boot 3.5)

B) Any Spring profile name that:
   - Contains a space character
   - Starts or ends with '-' (dash)
   - Starts or ends with '_' (underscore)
   (These are illegal in Spring Boot 3.5 and cause startup failure)

C) Any configuration block where both spring.data.redis.url AND
   spring.data.redis.database are set together
   (In Spring Boot 3.5, the 'database' property is silently ignored when 'url' is set)

D) Any deprecated Spring Boot property keys known to have been renamed
   between Spring Boot 3.1.x and 3.5.x

Report each violation as: FILE | PROPERTY KEY | CURRENT VALUE | REQUIRED FIX

════════════════════════════════════════
AUDIT 4 — BEAN NAME SCAN
════════════════════════════════════════
Search the entire codebase for all occurrences of the string "taskExecutor" used in:
- @Qualifier("taskExecutor")
- @Resource(name = "taskExecutor")
- @Async("taskExecutor")
- ApplicationContext.getBean("taskExecutor", ...)
- Any String literal "taskExecutor" in configuration classes

In Spring Boot 3.5.x, the auto-configured TaskExecutor bean name changes from
'taskExecutor' to 'applicationTaskExecutor'. Every occurrence must be updated.
Report each as: FILE PATH | LINE NUMBER | USAGE PATTERN

════════════════════════════════════════
AUDIT 5 — GATEWAY X-FORWARDED HEADER SCAN
════════════════════════════════════════
Search the codebase for any code or configuration that reads or relies on:
- X-Forwarded-Host
- X-Forwarded-Port
- X-Forwarded-Proto
- X-Forwarded-Prefix
- Forwarded (header)
- HttpServletRequest.getScheme()
- UriComponentsBuilder.fromCurrentRequest()
- ForwardedHeaderFilter
- ServerHttpRequest.getURI() combined with forwarded header logic

In Spring Cloud 2025.0.x, the Gateway stops forwarding these headers by default.
Any downstream service relying on them will silently return wrong URLs.
Report each finding as: FILE | LINE | USAGE | RISK LEVEL

════════════════════════════════════════
AUDIT 6 — OPENFEIGN CLIENT SCAN
════════════════════════════════════════
List all @FeignClient interfaces in the codebase. For each one, report:
- Interface name and file path
- Target service name
- Whether it uses @RequestMapping or @HttpExchange annotations
- Whether it has a custom RequestInterceptor
- Whether it has a custom ErrorDecoder
- Whether it has a custom fallback defined

════════════════════════════════════════
AUDIT 7 — DEPENDENCY VERSION RISK REPORT
════════════════════════════════════════
Read #file:pom.xml and identify the currently resolved versions of:
Flyway, Hibernate, Kafka Client, Jackson, Liquibase, Micrometer,
OpenTelemetry, and Resilience4j.

Compare against these Spring Boot 3.5.13 BOM target versions:
- Flyway        : 9.x  → 11.7.x   (MAJOR version jump — HIGH RISK)
- Hibernate     : 6.2.x → 6.4.x   (MEDIUM RISK)
- Kafka Client  : 3.4.x → 3.9.x   (MEDIUM RISK)
- Jackson       : 2.15.x → 2.19.x (MEDIUM RISK)
- Liquibase     : 4.20.x → 4.31.x (LOW RISK)
- Micrometer    : 1.11.x → 1.15.x (LOW RISK)
- OpenTelemetry : 1.28.x → 1.49.x (MEDIUM RISK)
- Resilience4j  : 2.1.x → Cloud-managed (MEDIUM RISK)

For each dependency found, report: DEPENDENCY | CURRENT VERSION | TARGET VERSION | RISK LEVEL | ACTION NEEDED

════════════════════════════════════════
AUDIT 8 — @CONFIGURATIONPROPERTIES NESTED VALIDATION SCAN
════════════════════════════════════════
Search the codebase for all classes annotated with @ConfigurationProperties
that are ALSO annotated with @Validated. For each one found:
- List every field in the class
- Flag fields that reference another @ConfigurationProperties nested class
  but are NOT annotated with @Valid on that field

In Spring Boot 3.4+, validation no longer cascades to nested fields automatically.
@Valid must be explicitly present on each nested field.
Report as: CLASS | FIELD NAME | FIELD TYPE | HAS @Valid | ACTION NEEDED

════════════════════════════════════════
EXPECTED OUTPUT FORMAT
════════════════════════════════════════
Present all findings as a structured audit report with one section per audit item.
Summarise total issues found at the end.
Do NOT make any code changes — this phase is analysis only.
```

---
---

# ══════════════════════════════════════════════════
# PHASE 1 — Spring Boot 3.1.0 → 3.2.x
#           Spring Cloud 2022.0.x → 2023.0.x
# Paste this entire block into GitHub Copilot Chat
# ══════════════════════════════════════════════════

```
I am upgrading this Spring Boot microservice from 3.1.0 to 3.2.x.
Read all files below before making any changes.

#file:pom.xml
#file:src/main/resources/application.yml
#file:src/main/resources/application-local.yml
#file:src/main/resources/application-docker.yml
#file:src/main/resources/application-prod.yml

════════════════════════════════════════
TASK 1 — UPDATE pom.xml VERSIONS
════════════════════════════════════════
In #file:pom.xml make the following version changes:

1. Update the spring-boot-starter-parent version to 3.2.13
2. Update the spring-cloud-dependencies BOM version to 2023.0.6
3. Ensure the spring-cloud.version property reflects 2023.0.6
4. Add the spring-boot-properties-migrator dependency in runtime scope
   (temporary — will be removed in Phase 5):
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-properties-migrator</artifactId>
       <scope>runtime</scope>
   </dependency>

Show me the complete updated pom.xml sections: <parent>, <properties>,
and <dependencyManagement>.

════════════════════════════════════════
TASK 2 — MARK ALL RestTemplate USAGES
════════════════════════════════════════
@workspace Find every class that injects or constructs RestTemplate.
For each occurrence, add a TODO comment on the injection point:
// TODO Phase 5: Migrate to RestClient (deprecated path — Spring Boot 3.2+)

Do NOT migrate to RestClient now. Only add the TODO marker.
Show me every file changed with the before and after for each change.

════════════════════════════════════════
TASK 3 — SPRING SECURITY FILTER CHAIN REVIEW
════════════════════════════════════════
@workspace Find all classes that define a SecurityFilterChain bean
or that extend WebSecurityConfigurerAdapter.

For each SecurityFilterChain bean found:
- Show me the current configuration
- Flag any use of the following patterns that have changed in Spring Security 6.2:
  * Method chains using and() between sections (now deprecated in favour of lambda DSL)
  * oauth2ResourceServer() configuration using method references instead of lambdas
  * Any csrf().disable() — should now be csrf(AbstractHttpConfigurer::disable)
  * Any cors() — should now be cors(withDefaults()) or cors(c -> c.configurationSource(...))

Show the before and after for each fix required.
Apply the lambda DSL refactoring wherever method-chain API is in use.

════════════════════════════════════════
TASK 4 — OPENFEIGN MICROMETER CONFLICT CHECK
════════════════════════════════════════
@workspace Find all classes that manually configure MicrometerObservationCapability
or FeignClientMetricsProperties.

Spring Cloud 2023.0.x auto-configures OpenFeign + Micrometer integration.
Manual configuration of these beans now conflicts with auto-configuration.
Remove any manual bean definitions for:
- MicrometerObservationCapability
- MicrometerCapability
- FeignClientMetricsProperties

Show me each file changed with before and after.

════════════════════════════════════════
TASK 5 — MICROMETER ObservationRegistry CONFLICT CHECK
════════════════════════════════════════
@workspace Find all @Bean methods that manually create or return an ObservationRegistry.
Spring Boot 3.2.x fully auto-configures ObservationRegistry.
Manual bean definitions for ObservationRegistry may now conflict.
Show me each one found and recommend whether it should be removed or adapted.

════════════════════════════════════════
EXPECTED OUTPUT FORMAT FOR EACH TASK
════════════════════════════════════════
For every change made:
1. File path
2. Before code block
3. After code block
4. One-line explanation of why the change is required
Do not change anything outside the scope of the tasks above.
```

> 💡 **After pasting:** Run `./mvnw clean verify`. If compilation fails,
> use `/fix` on the failing file in Copilot Chat.
> Do NOT proceed to Phase 2 until the build is 100% green.

---
---

# ══════════════════════════════════════════════════
# PHASE 2 — Spring Boot 3.2.x → 3.3.x
#           Spring Cloud stays on 2023.0.x
# Paste this entire block into GitHub Copilot Chat
# ══════════════════════════════════════════════════

```
I am upgrading this Spring Boot microservice from 3.2.x to 3.3.x.
Spring Cloud remains on 2023.0.6 — no BOM change needed for this phase.
Read all files below before making any changes.

#file:pom.xml
#file:src/main/resources/application.yml
#file:src/main/resources/application-local.yml
#file:src/main/resources/application-docker.yml
#file:src/main/resources/application-prod.yml

════════════════════════════════════════
TASK 1 — UPDATE pom.xml VERSION
════════════════════════════════════════
In #file:pom.xml update the spring-boot-starter-parent version to 3.3.12.
Spring Cloud BOM stays on 2023.0.6 — do not change it.
Show me the updated <parent> block only.

════════════════════════════════════════
TASK 2 — OCI IMAGE BUILDER VERIFICATION
════════════════════════════════════════
In #file:pom.xml check if the spring-boot-maven-plugin is configured
with an explicit image builder setting under the <build> section.

In Spring Boot 3.3.x, the default CNB builder changed from
'paketobuildpacks/builder-jammy-base' to 'paketobuildpacks/builder-jammy-tiny'.
The tiny builder does not include a shell — if the application uses a start script
this may cause image build failures.

If no explicit builder is configured, add this to the spring-boot-maven-plugin:
<configuration>
    <image>
        <builder>paketobuildpacks/builder-jammy-base</builder>
    </image>
</configuration>

Show the updated plugin configuration.

════════════════════════════════════════
TASK 3 — LOGBACK / LOG4J2 CUSTOM CONFIG CHECK
════════════════════════════════════════
Check if either of these files exist in the project:
#file:src/main/resources/logback-spring.xml
#file:src/main/resources/log4j2-spring.xml

If found, review the configuration for any syntax or element names that are
deprecated or changed in the Logback 1.4.x / Log4j2 2.21.x versions shipped
with Spring Boot 3.3.x. Report any warnings and apply the required fixes.

════════════════════════════════════════
TASK 4 — ACTUATOR ENDPOINT EXPOSURE AUDIT
════════════════════════════════════════
In all property files, find the management.endpoints.web.exposure.include setting.
Flag any profile where this is set to '*' (wildcard).

For each wildcard exposure found, replace it with an explicit list of
only the endpoints intentionally needed:
health, info, metrics, loggers, prometheus

Do NOT use wildcard exposure in docker or prod profiles.
Show me the before and after for each property file changed.

════════════════════════════════════════
EXPECTED OUTPUT FORMAT FOR EACH TASK
════════════════════════════════════════
For every change made:
1. File path
2. Before code block
3. After code block
4. One-line explanation of why the change is required
Do not change anything outside the scope of the tasks above.
```

> 💡 **After pasting:** Run `./mvnw clean verify`. Confirm all tests pass.
> Do NOT proceed to Phase 3 until the build is 100% green.

---
---

# ══════════════════════════════════════════════════
# PHASE 3 — Spring Boot 3.3.x → 3.4.x            ⚠️ HIGH RISK
#           Spring Cloud 2023.0.x → 2024.0.x
# Paste this entire block into GitHub Copilot Chat
# ══════════════════════════════════════════════════

```
I am upgrading this Spring Boot microservice from 3.3.x to 3.4.x.
Spring Cloud MUST be upgraded from 2023.0.x to 2024.0.x in this phase.
This is a HIGH RISK phase — deprecated APIs from 3.2.x are permanently removed
and WILL cause compilation failures if not resolved.

Read all files below before making any changes.

#file:pom.xml
#file:src/main/resources/application.yml
#file:src/main/resources/application-local.yml
#file:src/main/resources/application-docker.yml
#file:src/main/resources/application-prod.yml

════════════════════════════════════════
TASK 1 — UPDATE pom.xml VERSIONS
════════════════════════════════════════
In #file:pom.xml make the following changes:
1. Update spring-boot-starter-parent version to 3.4.6
2. Update spring-cloud-dependencies BOM version to 2024.0.3
3. Update the spring-cloud.version property to 2024.0.3

Show me the updated <parent>, <properties>, and <dependencyManagement> sections.

════════════════════════════════════════
TASK 2 — FIX REMOVED DEPRECATED APIS (COMPILATION BLOCKERS)
════════════════════════════════════════
After the version update in Task 1, the build will likely fail to compile.
All Spring Boot / Spring Security / Spring Data APIs deprecated in 3.2.x
are permanently removed in 3.4.x.

Run the build mentally against the findings from Phase 0 Audit 2.
For each deprecated API identified in the audit:
- Show the current usage
- Show the replacement API
- Apply the fix

Common patterns to fix in this phase:
- Security method-chain DSL → lambda DSL (if not done in Phase 1)
- Any Spring Data repository methods with deprecated signatures
- Any Spring Boot actuator APIs deprecated in 3.2

If the build fails after the version bump, use /fix on each failing file.
Show every changed file with before and after.

════════════════════════════════════════
TASK 3 — ADD @Valid TO ALL NESTED @ConfigurationProperties FIELDS
════════════════════════════════════════
This is a SILENT RUNTIME BUG if not fixed — no compilation error, validation just stops.

Using the findings from Phase 0 Audit 8, go to each @ConfigurationProperties class
annotated with @Validated that has nested property fields without @Valid.

For each such class:
- Add @Valid annotation on every field that references another nested properties class
- Ensure the javax.validation.Valid or jakarta.validation.Valid import is correct
  (Spring Boot 3.x uses jakarta.validation)

Example of the required fix:
BEFORE:
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    private ServiceProperties service;
    private DatabaseProperties database;
}

AFTER:
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    @Valid
    private ServiceProperties service;
    @Valid
    private DatabaseProperties database;
}

Show every class changed with before and after.
Use /tests to generate a @ConfigurationPropertiesTest for each fixed class to
verify validation cascades correctly.

════════════════════════════════════════
TASK 4 — FIX HTTP CLIENT REDIRECT BEHAVIOUR
════════════════════════════════════════
In Spring Boot 3.4.x, ALL HTTP clients (RestClient, RestTemplate, WebClient)
now follow redirects by default. Previously they did not.

Step 1: @workspace Find all outbound HTTP calls in the codebase that target
external APIs or OAuth2 endpoints that may return 3xx redirect responses.
List each call site with its target URL or service name.

Step 2: For calls where redirect-following is NOT desired (e.g., OAuth2 flows,
file download redirects), add the explicit no-redirect configuration to the
relevant property file:
spring:
  http:
    client:
      redirects: dont-follow

Apply this only to profiles where it is needed. Show before and after for
each property file changed.

════════════════════════════════════════
TASK 5 — FIX HtmlUnit DEPENDENCY COORDINATES (TEST SCOPE)
════════════════════════════════════════
In #file:pom.xml search for any dependency on net.sourceforge.htmlunit:htmlunit.
If found, update it:

BEFORE:
<dependency>
    <groupId>net.sourceforge.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <scope>test</scope>
</dependency>

AFTER:
<dependency>
    <groupId>org.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <scope>test</scope>
</dependency>

Also update the Selenium HtmlUnit driver if present:
BEFORE: org.seleniumhq.selenium:htmlunit-driver
AFTER:  org.seleniumhq.selenium:htmlunit3-driver

@workspace Find all Java test files importing com.gargoylesoftware.htmlunit.*
and update all imports to org.htmlunit.*
Show every changed file with before and after.

════════════════════════════════════════
TASK 6 — SPRING CLOUD OPENFEIGN ALIGNMENT FOR 2024.0.x
════════════════════════════════════════
@workspace Find all @FeignClient interfaces identified in Phase 0 Audit 6.

For each interface, check:
1. If it uses @RequestMapping annotations — these still work but ensure compatibility
   with Spring Cloud 2024.0.x OpenFeign + Spring Framework 6.1 HTTP interface model
2. If it has a custom RequestInterceptor or ErrorDecoder — verify the constructor
   signatures and bean registration are compatible with 2024.0.x auto-configuration

Show any interfaces that require changes with before and after code.

════════════════════════════════════════
TASK 7 — SPRING CLOUD GATEWAY ROUTE REVIEW FOR 2024.0.x
════════════════════════════════════════
@workspace Find all Spring Cloud Gateway route definitions in:
- Java configuration classes (RouteLocatorBuilder)
- Property files (spring.cloud.gateway.routes)

Check each route for:
- Deprecated predicate or filter names that have been updated in 2024.0.x
- Any WeightRoutePredicateFactory, CloudFoundryRouteServiceRoutePredicateFactory
  or other predicates known to have changed in Spring Cloud 2024.0.x

Report deprecated route definitions with the updated replacements.

════════════════════════════════════════
TASK 8 — EUREKA METADATA AND REGISTRATION VERIFICATION
════════════════════════════════════════
In all property files, find the eureka.instance.* and eureka.client.* configuration.
Verify the following settings are still correctly defined for Spring Cloud 2024.0.x:
- eureka.instance.prefer-ip-address
- eureka.instance.lease-renewal-interval-in-seconds
- eureka.instance.lease-expiration-duration-in-seconds
- eureka.client.registry-fetch-interval-seconds
- eureka.instance.metadata-map (custom metadata)

Report any property keys that have been deprecated or renamed in 2024.0.x.

════════════════════════════════════════
EXPECTED OUTPUT FORMAT FOR EACH TASK
════════════════════════════════════════
For every change made:
1. File path
2. Before code block
3. After code block
4. One-line explanation of why the change is required
Do not change anything outside the scope of the tasks above.
After all tasks are complete, list any remaining compilation errors for /fix resolution.
```

> 💡 **If compilation fails after Task 1:** Use `/fix` on each failing file.
> The root cause is always removed deprecated APIs — fix those in Task 2 first.
> Do NOT proceed to Phase 4 until `./mvnw clean verify` is 100% green.

---
---

# ══════════════════════════════════════════════════
# PHASE 4 — Spring Boot 3.4.x → 3.5.13           ⚠️ HIGH RISK
#           Spring Cloud 2024.0.x → 2025.0.x
# Paste this entire block into GitHub Copilot Chat
# ══════════════════════════════════════════════════

```
I am upgrading this Spring Boot microservice from 3.4.x to 3.5.13.
Spring Cloud MUST be upgraded from 2024.0.x to 2025.0.x in this phase.
This is a HIGH RISK phase — security defaults, bean names, profile naming,
and Spring Cloud Gateway behaviour all change.

Read all files below before making any changes.

#file:pom.xml
#file:src/main/resources/application.yml
#file:src/main/resources/application-local.yml
#file:src/main/resources/application-docker.yml
#file:src/main/resources/application-prod.yml

════════════════════════════════════════
TASK 1 — UPDATE pom.xml VERSIONS
════════════════════════════════════════
In #file:pom.xml make the following changes:
1. Update spring-boot-starter-parent version to 3.5.13
2. Update spring-cloud-dependencies BOM version to 2025.0.1
3. Update the spring-cloud.version property to 2025.0.1

Show me the updated <parent>, <properties>, and <dependencyManagement> sections.

════════════════════════════════════════
TASK 2 — RENAME taskExecutor → applicationTaskExecutor (CRITICAL)
════════════════════════════════════════
In Spring Boot 3.5.x, the auto-configured TaskExecutor bean is registered ONLY under
the name 'applicationTaskExecutor'. The name 'taskExecutor' no longer exists.
Any injection using 'taskExecutor' by name will throw NoSuchBeanDefinitionException at runtime.

Using the findings from Phase 0 Audit 4, update every occurrence found:

Pattern A — @Qualifier injection:
BEFORE: @Qualifier("taskExecutor")
AFTER:  @Qualifier("applicationTaskExecutor")

Pattern B — @Resource by name:
BEFORE: @Resource(name = "taskExecutor")
AFTER:  @Resource(name = "applicationTaskExecutor")

Pattern C — @Async executor name:
BEFORE: @Async("taskExecutor")
AFTER:  @Async("applicationTaskExecutor")

Pattern D — ApplicationContext.getBean by name:
BEFORE: context.getBean("taskExecutor", TaskExecutor.class)
AFTER:  context.getBean("applicationTaskExecutor", TaskExecutor.class)

IMPORTANT: If the project defines its OWN @Bean named taskExecutor (custom bean,
not relying on auto-configuration), do NOT rename it — only the auto-configured
bean is affected. Confirm this distinction before making changes.

Show every changed file with before and after for each occurrence.

════════════════════════════════════════
TASK 3 — FIX heapdump ACTUATOR ENDPOINT ACCESS (CRITICAL)
════════════════════════════════════════
In Spring Boot 3.5.x, the heapdump actuator endpoint defaults to access=NONE.
Exposing it in exposure.include alone is no longer sufficient.

In all property files, search for heapdump in the management configuration.
If the heapdump endpoint is exposed, add the explicit access configuration:

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, loggers, heapdump
  endpoint:
    heapdump:
      access: unrestricted

If heapdump is NOT exposed in any profile, no action is needed — the new default
is a security improvement.

Show the before and after for each property file changed.

════════════════════════════════════════
TASK 4 — FIX .enabled PROPERTY VALUES (CRITICAL)
════════════════════════════════════════
In Spring Boot 3.5.x, only the exact strings 'true' or 'false' are accepted
for .enabled properties. Any other value causes a BindException at startup.

Using the findings from Phase 0 Audit 3 (item A), fix every .enabled property
whose value is not exactly 'true' or 'false':

Examples of invalid values that must be fixed:
  yes   → true
  no    → false
  1     → true
  0     → false
  on    → true
  off   → false
  TRUE  → true   (must be lowercase)
  FALSE → false  (must be lowercase)

Show before and after for each property file changed.

════════════════════════════════════════
TASK 5 — FIX PROFILE NAMES (CRITICAL)
════════════════════════════════════════
In Spring Boot 3.5.x, profile names must contain only letters, digits,
'-' (dash), or '_' (underscore). They cannot start or end with '-' or '_'.
Violation causes startup failure.

Using the findings from Phase 0 Audit 3 (item B), fix every invalid profile name in:
- All property files (spring.profiles.active, spring.profiles.include)
- @Profile("...") annotations across the codebase
- Any environment variable definitions in docker-compose.yml or Kubernetes manifests

If docker-compose.yml or Kubernetes manifests exist, check them too:
#file:docker-compose.yml
#file:docker-compose-local.yml
#file:k8s/deployment.yml

Show before and after for each file changed.

════════════════════════════════════════
TASK 6 — FIX Redis URL + DATABASE CONFLICT (CRITICAL)
════════════════════════════════════════
In Spring Boot 3.5.x, when spring.data.redis.url is configured,
the spring.data.redis.database property is silently ignored.
The database must be embedded as a path segment in the URL itself.

Using the findings from Phase 0 Audit 3 (item C), fix every property file
where both spring.data.redis.url and spring.data.redis.database are configured:

BEFORE:
spring:
  data:
    redis:
      url: redis://redis-host:6379
      database: 1

AFTER:
spring:
  data:
    redis:
      url: redis://redis-host:6379/1
      # database property removed — index embedded in URL as /N path segment

Show before and after for each property file changed.

════════════════════════════════════════
TASK 7 — CONFIGURE WebClient REDIRECT BEHAVIOUR
════════════════════════════════════════
In Spring Boot 3.5.x, WebClient now follows redirects by default
(aligning with the blocking client default change introduced in 3.4.x).

@workspace Find all WebClient beans and WebClient.Builder usages in the codebase.
If any WebClient is used to call APIs that should NOT follow redirects
(e.g., OAuth2 token endpoints, file download APIs, webhook callbacks), add:

spring:
  http:
    client:
      redirects: dont-follow

Apply only to profiles where this is needed.
Show before and after for each property file changed.

════════════════════════════════════════
TASK 8 — FIX @DependsOn FOR BACKGROUND BEAN INITIALISATION
════════════════════════════════════════
Spring Boot 3.5.x enables bean background initialisation by default via
the auto-configured bootstrapExecutor bean. Beans that previously relied on
sequential initialisation order may now initialise in parallel.

@workspace Find all beans annotated with @Lazy or beans that have explicit
@DependsOn annotations. Also find beans that use ApplicationListener<ContextRefreshedEvent>
or SmartInitializingSingleton to run logic after context startup.

For any bean that MUST be fully initialised before another bean uses it,
verify @DependsOn is correctly declared. Add it where it is missing.

Report each bean that may be affected and show any @DependsOn additions needed.

════════════════════════════════════════
TASK 9 — SPRING CLOUD GATEWAY BREAKING CHANGES FOR 2025.0.x (CRITICAL)
════════════════════════════════════════
Spring Cloud Gateway 2025.0.x (Northfields) introduces two critical breaking changes.

BREAKING CHANGE A — X-Forwarded-* Headers Disabled by Default:
In Spring Cloud 2025.0.x, the gateway NO LONGER forwards X-Forwarded-Host,
X-Forwarded-Port, X-Forwarded-Proto, X-Forwarded-Prefix, or Forwarded headers
to downstream services by default. This is a security improvement.

Using the findings from Phase 0 Audit 5, determine whether downstream services
rely on these headers. If they do, configure trusted proxies in the gateway
property file. Apply to the profile where the gateway is active (typically docker/prod):

spring:
  cloud:
    gateway:
      server:
        webflux:
          trusted-proxies: "10\\.0\\.0\\..*"   # Replace with your actual proxy IP regex

If downstream services do NOT rely on forwarded headers, no action is needed.
Show the before and after for each property file changed.

BREAKING CHANGE B — Gateway Starter Artifact Name Changes:
The spring-cloud-starter-gateway artifact is deprecated in 2025.0.x.
New artifact names were introduced to clarify the two gateway styles.

In #file:pom.xml find and replace the gateway starter dependency:

BEFORE (deprecated):
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

AFTER — For Reactive WebFlux-based gateway (most common):
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>

AFTER — For MVC Proxy Exchange-based gateway (if using WebMVC style):
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-mvc</artifactId>
</dependency>

Determine which style is in use based on the existing codebase and apply the
correct replacement. Show the updated pom.xml dependency block.

════════════════════════════════════════
TASK 10 — UPDATE DEPRECATED GATEWAY PROPERTY PREFIXES FOR 2025.0.x
════════════════════════════════════════
In all property files, search for any spring.cloud.gateway.routes configuration.
Spring Cloud 2025.0.x introduces new property prefix scoping under server.webflux.*

Check if the property-migrator logs indicate any deprecated gateway property keys.
If deprecated prefixes are found, update them:

BEFORE (old prefix):
spring:
  cloud:
    gateway:
      routes: ...

AFTER (new prefix for WebFlux gateway):
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes: ...

Apply this migration to all gateway-related properties in all property files.
Show before and after for each file changed.

════════════════════════════════════════
TASK 11 — FLYWAY MAJOR VERSION MIGRATION (CRITICAL)
════════════════════════════════════════
Spring Boot 3.5.13 BOM targets Flyway 11.7.x.
Your project is currently on Flyway 9.x — this is a MAJOR version jump (9 → 11).

Read:
#file:src/main/resources/db/migration/

Step 1: List all migration scripts present and their naming convention.

Step 2: Check if any Java-based Flyway callbacks or migrations exist:
@workspace Find all classes implementing FlywayCallback, BaseFlywayCallback,
JavaMigration, or BaseJavaMigration.

Step 3: For any Java-based migration or callback found, verify:
- FlywayCallback interface signature changed in Flyway 10 — the event parameter
  type is now org.flywaydb.core.api.callback.Event (not Context)
- JavaMigration implementations must now return a proper ChecksumCalculator
- BaseFlywayCallback was removed — must implement FlywayCallback directly

Step 4: In #file:pom.xml verify no explicit flyway.version property is pinning
Flyway to a version older than 11.7.0. If found, remove the pin and let the
Spring Boot 3.5.13 BOM manage it.

Step 5: Add the following Flyway properties to prevent analytics telemetry:
spring:
  flyway:
    out-of-order: false
    baseline-on-migrate: false

Show all files changed with before and after.

════════════════════════════════════════
TASK 12 — JACKSON 2.19.x COMPATIBILITY CHECK
════════════════════════════════════════
Spring Boot 3.5.13 brings Jackson 2.19.x (up from 2.15.x in 3.1.x).
Spring Cloud 2025.0.0 was specifically released to align with Jackson 2.19.x.

Step 1: In #file:pom.xml verify no explicit jackson.version property pins Jackson
to a version older than 2.19.x. If a pin exists, remove it and let the BOM manage.

Step 2: @workspace Find all classes that:
- Define a custom Jackson2ObjectMapperBuilderCustomizer bean
- Define a custom ObjectMapper bean
- Register a custom Jackson Module (implement com.fasterxml.jackson.databind.Module)
- Use ObjectMapper.configure() with deprecated settings

For each found, verify compatibility with Jackson 2.19.x.
Report any API usages that have changed and show the required fixes.

════════════════════════════════════════
TASK 13 — LIQUIBASE 4.31.x PROPERTY UPDATE
════════════════════════════════════════
Spring Boot 3.5.13 BOM targets Liquibase 4.31.x.
Two new Liquibase properties are available. Add these to all property files
to explicitly disable telemetry (recommended for production):

spring:
  liquibase:
    analytics-enabled: false

Do NOT add spring.liquibase.license-key unless using Liquibase Pro.
Show the before and after for each property file changed.

════════════════════════════════════════
TASK 14 — EUREKA CLIENT VERIFICATION FOR 2025.0.x
════════════════════════════════════════
@workspace Find the Eureka client configuration class if one exists
(typically implements EurekaInstanceConfigBeanCustomizer or defines an
EurekaInstanceConfigBean bean).

Verify:
1. Custom EurekaInstanceConfigBean customisers still apply correctly with 2025.0.x
2. eureka.instance.health-check-url-path is correctly set if custom health is used
3. The Eureka heartbeat and expiry intervals are still explicitly configured

Show any configuration that needs updating with before and after.

════════════════════════════════════════
TASK 15 — SPRING CLOUD CONFIG CLIENT VERIFICATION FOR 2025.0.x
════════════════════════════════════════
In all property files, find the spring.cloud.config.* configuration.
Verify:
1. spring.config.import=configserver: is present (required since Spring Boot 3.x)
2. Retry configuration is explicitly set (do not rely on defaults):
   spring:
     cloud:
       config:
         fail-fast: true
         retry:
           max-attempts: 6
           initial-interval: 1000
           multiplier: 1.1
           max-interval: 2000

Show the before and after for each property file where changes are needed.

════════════════════════════════════════
EXPECTED OUTPUT FORMAT FOR EACH TASK
════════════════════════════════════════
For every change made:
1. File path
2. Before code block
3. After code block
4. One-line explanation of why the change is required
5. Severity: CRITICAL / HIGH / MEDIUM / LOW

After all tasks are complete, produce a summary table of all changes made,
grouped by file name.
Do not change anything outside the scope of the tasks above.
```

> 💡 **After pasting:** Run `./mvnw clean verify`. If startup fails, check:
> - `BindException` → Task 4 (enabled values) or Task 5 (profile names)
> - `NoSuchBeanDefinitionException: taskExecutor` → Task 2
> - `403 on /actuator/heapdump` → Task 3
> - `Application startup veto` → Spring Cloud BOM version mismatch (Task 1)
>
> Use `/fix` on each failing file for targeted resolution.
> Do NOT proceed to Phase 5 until `./mvnw clean verify` is 100% green.

---
---

# ══════════════════════════════════════════════════
# PHASE 5 — POST-UPGRADE TASKS
# Paste this entire block into GitHub Copilot Chat
# ══════════════════════════════════════════════════

```
The Spring Boot upgrade to 3.5.13 and Spring Cloud 2025.0.x is functionally complete.
Now perform the following cleanup and optimisation tasks.

Read all files below before making any changes.

#file:pom.xml
#file:src/main/resources/application.yml
#file:src/main/resources/application-local.yml
#file:src/main/resources/application-docker.yml
#file:src/main/resources/application-prod.yml

════════════════════════════════════════
TASK 1 — REMOVE spring-boot-properties-migrator
════════════════════════════════════════
In #file:pom.xml remove the following dependency entirely:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-properties-migrator</artifactId>
    <scope>runtime</scope>
</dependency>

This dependency was added temporarily in Phase 1 and must not ship to production.
Show the updated pom.xml <dependencies> section confirming its removal.

════════════════════════════════════════
TASK 2 — MIGRATE RestTemplate TO RestClient
════════════════════════════════════════
@workspace Find all TODO comments added in Phase 1 with the pattern:
// TODO Phase 5: Migrate to RestClient

For each location found, migrate the RestTemplate usage to RestClient.

Migration pattern:
BEFORE:
@Autowired
private RestTemplate restTemplate;

public ResponseEntity<MyResponse> callService(String url) {
    return restTemplate.getForEntity(url, MyResponse.class);
}

AFTER:
private final RestClient restClient;

public MyServiceClient(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder
        .baseUrl("${service.base-url}")
        .build();
}

public MyResponse callService() {
    return restClient.get()
        .uri("/endpoint")
        .retrieve()
        .body(MyResponse.class);
}

Apply the migration to each location. Use /tests to generate or update
unit tests for each migrated class to verify correct behaviour.

Show every changed file with before and after.

════════════════════════════════════════
TASK 3 — FINAL DEPRECATION SCAN ON 3.5.13
════════════════════════════════════════
@workspace Scan the entire codebase for any Spring Boot, Spring Cloud,
Spring Security, or Spring Data APIs that are newly deprecated in 3.5.x.

For each deprecation found:
- Report: FILE | CLASS | METHOD | DEPRECATED IN | REPLACEMENT
- Do NOT fix these now — document them in a comment block at the top of
  each affected file:
  // TECH-DEBT: [ClassName.method] deprecated in Spring Boot 3.5.x
  //            Replace with [replacement] before next upgrade cycle.

════════════════════════════════════════
TASK 4 — UPGRADE JAVA VERSION TO 21
════════════════════════════════════════
Spring Boot 3.5.13 supports Java 17, 21, and 24.
Upgrade the project to Java 21 (LTS).

Step 1: In #file:pom.xml update:
<properties>
    <java.version>21</java.version>
</properties>

Step 2: Add Virtual Threads opt-in to the base application.yml:
spring:
  threads:
    virtual:
      enabled: true

Step 3: @workspace Find all places where platform thread pool sizes are
hardcoded (e.g., ThreadPoolTaskExecutor with setCorePoolSize, setMaxPoolSize).
With Virtual Threads, fixed pool sizes are no longer optimal.
Report each location and recommend whether the pool configuration should
be removed or adapted for Virtual Thread usage.

Step 4: Verify the Docker base image for the project needs updating.
Check if a Dockerfile exists:
#file:Dockerfile
#file:Dockerfile.local

If found, update the base image to Java 21:
FROM eclipse-temurin:21-jre-jammy
# or for smaller images:
FROM eclipse-temurin:21-jre-alpine

Show all changed files with before and after.

════════════════════════════════════════
TASK 5 — SPRING CLOUD GATEWAY STARTER FINAL CLEANUP
════════════════════════════════════════
Confirm that #file:pom.xml no longer contains:
<artifactId>spring-cloud-starter-gateway</artifactId>

And confirms the new artifact name from Phase 4 Task 9 is in place.
If spring-cloud-starter-parent is still referenced anywhere as a Maven parent,
remove it and replace with spring-boot-starter-parent + BOM import.
This artifact is deprecated in Spring Cloud 2025.0.x.

════════════════════════════════════════
TASK 6 — GENERATE UPGRADE COMPLETION TEST SUITE
════════════════════════════════════════
@workspace Use /tests to generate or update integration tests that verify:

1. All @ConfigurationProperties beans bind without errors on all profiles
2. The Eureka client registers and deregisters correctly
3. All OpenFeign clients make calls with correct timeout and retry behaviour
4. The Resilience4j circuit breaker opens and recovers correctly
5. The Flyway migration runs cleanly on a fresh empty schema
6. All actuator endpoints respond: /health, /info, /metrics, /loggers
7. The Redis connection uses the correct database index
8. All @Async methods execute using the applicationTaskExecutor correctly

Show the generated or updated test class for each item.

════════════════════════════════════════
TASK 7 — FINAL VERIFICATION COMMAND SEQUENCE
════════════════════════════════════════
Generate the following shell script to run the complete final verification.
Save it as verify-upgrade.sh in the project root:

#!/bin/bash
set -e
echo "=== Spring Boot 3.5.13 Upgrade — Final Verification ==="

echo "Step 1: Full clean build"
./mvnw clean verify -Dspring-boot.run.profiles=local

echo "Step 2: Deprecation scan"
./mvnw clean compile -Xlint:deprecation 2>&1 | grep "warning.*deprecated" | sort | uniq

echo "Step 3: Start on local profile and verify startup"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local &
APP_PID=$!
sleep 20
curl -f http://localhost:8080/actuator/health || (kill $APP_PID && exit 1)
curl -f http://localhost:8080/actuator/info   || (kill $APP_PID && exit 1)
curl -f http://localhost:8080/actuator/metrics || (kill $APP_PID && exit 1)
kill $APP_PID

echo "Step 4: Docker image build"
./mvnw spring-boot:build-image

echo "=== All verification steps passed ==="

Show the complete script content.

════════════════════════════════════════
EXPECTED OUTPUT FORMAT FOR EACH TASK
════════════════════════════════════════
For every change made:
1. File path
2. Before code block
3. After code block
4. One-line explanation of why the change is required
Do not change anything outside the scope of the tasks above.
```

---
---

## QUICK REFERENCE — Most Common Failure Modes After This Upgrade

| Symptom at Startup / Runtime                        | Root Cause                                | Copilot Action            | Fix                                                |
|-----------------------------------------------------|-------------------------------------------|---------------------------|----------------------------------------------------|
| `NoSuchBeanDefinitionException: taskExecutor`       | Phase 4.2 — bean name removed             | `/fix` on failing class   | Change to `applicationTaskExecutor`                |
| `403 Forbidden` on `/actuator/heapdump`             | Phase 4.3 — access defaults to NONE       | Update property file      | Set `endpoint.heapdump.access=unrestricted`        |
| `BindException` on startup for `.enabled`           | Phase 4.4 — strict boolean enforcement    | Update property file      | Set value to exactly `true` or `false`             |
| `IllegalArgumentException: invalid profile name`    | Phase 4.5 — profile naming enforced       | Update property file      | Remove spaces / leading / trailing dash            |
| Wrong Redis database being used                     | Phase 4.6 — `database` ignored with URL  | Update property file      | Embed database index as `/N` in URL                |
| `CompilationFailure` after Phase 3 bump             | Phase 3.2 — removed deprecated APIs      | `/fix` on each failure    | Fix all 3.2.x deprecations before Phase 3          |
| Nested `@ConfigurationProperties` validation silent | Phase 3.3 — `@Valid` not cascading        | `/fix` + `/tests`         | Add `@Valid` on nested property fields             |
| External API calls fail / unexpected redirects      | Phase 3.4 / 4.7 — redirect defaults      | Update property file      | Set `spring.http.client.redirects=dont-follow`     |
| Flyway migration fails at startup                   | Phase 4.11 — Flyway 11.x major jump       | `/fix` on callback class  | Update callback interfaces for Flyway 10/11 API    |
| Downstream services return wrong scheme or host     | Phase 4.9A — X-Forwarded headers disabled | Update gateway properties | Configure `trusted-proxies` in gateway config      |
| `Application startup veto` from Spring Cloud        | Spring Cloud / Boot version mismatch      | Check pom.xml             | Boot 3.5.x MUST pair with Cloud 2025.0.x           |
| Feign client `NoSuchMethodError` at runtime         | Spring Cloud BOM version mismatch         | Check pom.xml             | Ensure 2025.0.x BOM is active                     |
| Jackson `MismatchedInputException` in Cloud routes  | Phase 4.12 — Jackson 2.19.x              | `/fix` on ObjectMapper    | Remove pinned Jackson version; let BOM manage      |
| Gateway route `404` after upgrade                   | Phase 4.10 — deprecated property prefix  | Update property file      | Migrate to `server.webflux.*` property prefix      |
| Gateway route `404` with wrong artifact             | Phase 4.9B — deprecated starter name     | Update pom.xml            | Use `spring-cloud-starter-gateway-server-webflux`  |
