# Spring Boot Upgrade Prompt: 3.1.0 → 3.5.13 (with Spring Cloud)
> **Target Spring Boot Version :** 3.5.13
> **Scope                      :** Production-grade microservice with Spring Cloud (Eureka, Config, Gateway, OpenFeign, Circuit Breaker)
> **Strategy                   :** Phased, hop-by-hop — never jump directly from 3.1.0 to 3.5.13 in one commit
> **Build Tool                 :** Maven

---

## SPRING CLOUD RELEASE TRAIN COMPATIBILITY MAP
> ⚠️ CRITICAL — Mismatching Spring Boot and Spring Cloud versions causes runtime failures
> (`NoSuchMethodError`, `ClassNotFoundException`, startup veto errors).
> You MUST upgrade the Spring Cloud BOM at every phase in lock-step with the Spring Boot version.

| Spring Boot Version | Required Spring Cloud Train | Codename     | Latest Patch  |
|---------------------|-----------------------------|--------------|---------------|
| 3.1.x               | 2022.0.x                    | Kilburn      | 2022.0.x (EOL)|
| 3.2.x / 3.3.x      | 2023.0.x                    | Leyton       | 2023.0.6      |
| 3.4.x               | 2024.0.x                    | Moorgate     | 2024.0.3      |
| 3.5.x               | 2025.0.x                    | Northfields  | 2025.0.1      |

> Spring Cloud 2025.0.1 is based on Spring Boot 3.5.8 and is fully compatible with 3.5.13.

---

## UPGRADE PHASE OVERVIEW

```
Phase 0: Pre-Upgrade Audit    (no version change — codebase-wide analysis)
Phase 1: 3.1.0  → 3.2.x      Spring Cloud 2022.0.x → 2023.0.x
Phase 2: 3.2.x  → 3.3.x      Spring Cloud 2023.0.x (stay on same train)
Phase 3: 3.3.x  → 3.4.x      Spring Cloud 2023.0.x → 2024.0.x  ⚠️ HIGH RISK
Phase 4: 3.4.x  → 3.5.13     Spring Cloud 2024.0.x → 2025.0.x  ⚠️ HIGH RISK
Phase 5: Post-Upgrade Tasks   (cleanup, Java 21, final verification)
```

> After each phase: run `./mvnw clean verify`, start on all profiles, and confirm
> a 100% green build before proceeding to the next phase.

---

## PHASE 0 — PRE-UPGRADE AUDIT
> Perform this entire section BEFORE touching any version in pom.xml.

### 0.1 Deprecated API Full Scan
- Run the Maven build with `-Xlint:deprecation` to surface all deprecation warnings:
  ```bash
  ./mvnw clean compile -Xlint:deprecation 2>&1 | grep "warning.*deprecated" | sort | uniq
  ```
- Document EVERY deprecated class, method, and property in Spring Boot, Spring Framework,
  Spring Security, Spring Data, and Spring Cloud.
- These WILL be removed in Phase 3 (3.4.x removes all APIs deprecated in 3.2.x).
  They must be resolved before Phase 3 begins.

### 0.2 Spring Cloud Component Audit
Identify which Spring Cloud components are in use across the microservice:
```
[ ] spring-cloud-starter-netflix-eureka-client    (service discovery)
[ ] spring-cloud-starter-netflix-eureka-server    (if this service IS the registry)
[ ] spring-cloud-starter-config                   (config client)
[ ] spring-cloud-config-server                    (if this service IS the config server)
[ ] spring-cloud-starter-gateway                  (API gateway)
[ ] spring-cloud-starter-openfeign                (declarative HTTP client)
[ ] spring-cloud-starter-circuitbreaker-resilience4j (circuit breaker)
[ ] spring-cloud-starter-sleuth / micrometer-tracing (distributed tracing)
[ ] spring-cloud-bus                              (distributed event bus)
[ ] spring-cloud-stream                           (messaging abstraction)
[ ] spring-cloud-kubernetes                       (Kubernetes integration)
```
This checklist drives what Spring Cloud-specific fixes are needed in each phase.

### 0.3 Property File Full Audit
Scan ALL property files: `application.yml`, `application.properties`,
`application-local.yml`, `application-docker.yml`, `application-prod.yml` for:

- **`.enabled=` values** — must be strictly `true` or `false` (enforced in 3.5).
- **Profile names** — must only contain letters, digits, `-` (dash), `_` (underscore).
  No spaces, no leading/trailing dash or underscore (enforced in 3.5).
- **`spring.data.redis.database`** alongside `spring.data.redis.url` (ignored in 3.5 — database must be embedded in URL).
- **Deprecated property keys** — add this dependency temporarily to auto-detect them at startup:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-properties-migrator</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```
  The migrator logs warnings for every deprecated key with its replacement. Remove this
  dependency before the final production build.

### 0.4 Bean Name Audit
- Search for any injection or lookup using the bean name `taskExecutor` as a string literal,
  `@Qualifier("taskExecutor")`, `@Resource(name = "taskExecutor")`, or `@Async("taskExecutor")`.
  In Spring Boot 3.5.x, only `applicationTaskExecutor` is auto-registered.
  Every occurrence must be changed to `applicationTaskExecutor`.

### 0.5 Spring Cloud Gateway X-Forwarded Header Audit
- If your microservice uses Spring Cloud Gateway, check whether any routes depend on
  `X-Forwarded-*` or `Forwarded` headers being forwarded from the gateway to downstream services.
  In Spring Cloud 2025.0.x, this behaviour is DISABLED by default (security improvement).
  Identify all routes and load balancers that rely on these headers.

### 0.6 OpenFeign Client Audit
- List all `@FeignClient` interfaces. In Spring Cloud 2023.0.x onward, OpenFeign
  integrates with Micrometer for observability. Verify that custom `RequestInterceptor`
  and `ErrorDecoder` beans do not conflict with new auto-configured instrumentation.

### 0.7 Dependency Compatibility Pre-Check
Before upgrading, verify the versions of these key dependencies in your current pom.xml
and flag any that will require code-level changes (not just version number updates):

| Dependency       | Your 3.1.x Version | 3.5.13 BOM Target | Risk Level |
|------------------|--------------------|-------------------|------------|
| Flyway           | 9.x                | 11.x              | 🔴 HIGH    |
| Hibernate        | 6.2.x              | 6.4.x             | 🟠 MEDIUM  |
| Kafka Client     | 3.4.x              | 3.9.x             | 🟠 MEDIUM  |
| Jackson          | 2.15.x             | 2.19.x            | 🟠 MEDIUM  |
| Liquibase        | 4.20.x             | 4.31.x            | 🟡 LOW     |
| Micrometer       | 1.11.x             | 1.15.x            | 🟡 LOW     |
| OpenTelemetry    | 1.28.x             | 1.49.x            | 🟠 MEDIUM  |
| Resilience4j     | 2.1.x              | managed by Cloud  | 🟠 MEDIUM  |

### 0.8 Establish Test Baseline
- Run the full test suite and record the pass/fail counts. This is your baseline.
  Every phase must end with identical or improved pass counts before proceeding:
  ```bash
  ./mvnw clean verify | tail -20
  ```

---

## PHASE 1 — Spring Boot 3.1.0 → 3.2.x | Spring Cloud 2022.0.x → 2023.0.x

### pom.xml Changes
```xml
<!-- Spring Boot parent -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.13</version>
    <relativePath/>
</parent>

<!-- Spring Cloud BOM — inside <dependencyManagement> -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.6</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Recommended: track the version in a property -->
<properties>
    <spring-cloud.version>2023.0.6</spring-cloud.version>
    <java.version>17</java.version>
</properties>
```

### 1.1 Spring Boot Breaking Changes in This Phase

#### Virtual Threads (Opt-In — Do Not Enable Yet)
- Spring Boot 3.2 introduces opt-in Virtual Thread support via:
  ```yaml
  spring:
    threads:
      virtual:
        enabled: true
  ```
- Do NOT enable yet. If you have custom `ThreadPoolTaskExecutor` beans with fixed pool sizes,
  Virtual Threads will change concurrency behaviour. Enable only after full testing in Phase 5.

#### RestClient Introduced — RestTemplate Soft Deprecation Begins
- `RestTemplate` is not yet removed, but `RestClient` is the new preferred API.
- Add `// TODO: Migrate to RestClient` comments on all `RestTemplate` injection points.
- Do NOT migrate now — only document locations for a future follow-up ticket.

#### Spring Security 6.2 — SecurityFilterChain Review
- Review all `SecurityFilterChain` beans. The `permitAll()` and `authenticated()` chain
  ordering is more strictly validated.
- If using `OAuth2ResourceServerConfigurer`, check for method-chain API changes.
- Verify JWT decoder and converter configuration still works with the updated security auto-config.

#### Micrometer 1.12 — Observability API
- If using `Observation` API directly via `ObservationRegistry`, review API changes.
- Remove any manual `ObservationRegistry` bean definitions — auto-configuration is now
  more complete and custom beans may conflict.

### 1.2 Spring Cloud Breaking Changes in This Phase (2022.0.x → 2023.0.x)

#### Spring Cloud Config Client
- The `spring.cloud.config.fail-fast` property default behaviour is unchanged,
  but the retry configuration has been updated. If you have custom config retry settings,
  verify them:
  ```yaml
  spring:
    cloud:
      config:
        retry:
          max-attempts: 6
          initial-interval: 1000
          multiplier: 1.1
          max-interval: 2000
  ```

#### OpenFeign — Micrometer Instrumentation Auto-Configured
- OpenFeign now auto-configures Micrometer instrumentation in Spring Cloud 2023.0.x.
- If you have manually configured `FeignClientMetricsProperties` or a custom
  `MicrometerObservationCapability` bean, remove the manual configuration to avoid conflicts.

#### Spring Cloud Gateway — Remove Deprecated Routes
- Any gateway routes using deprecated predicates or filters from Spring Cloud 2022.0.x
  should be updated. Review the gateway route definitions in config files.

#### Resilience4j Circuit Breaker
- Spring Cloud 2023.0.x moves Resilience4j integration to use the `ObservationRegistry`
  from Micrometer. If you have custom `CircuitBreakerConfigCustomizer` beans,
  verify they still function correctly.

#### Eureka Client — Health Check Behaviour
- Eureka client health check contribution behaviour is updated.
- Verify your service still registers and deregisters correctly from Eureka on startup/shutdown.
- Test `@EurekaClient` or `DiscoveryClient` injections still resolve correctly.

### Phase 1 Validation Checklist
```
[ ] ./mvnw clean verify — zero compilation errors, zero test failures
[ ] Application starts on all profiles (local, docker, prod)
[ ] Service registers in Eureka (if applicable)
[ ] Config values are loaded from Config Server (if applicable)
[ ] OpenFeign clients resolve target services successfully
[ ] Actuator /health, /info, /metrics respond correctly
[ ] No deprecated property warnings from spring-boot-properties-migrator
```

---

## PHASE 2 — Spring Boot 3.2.x → 3.3.x | Spring Cloud 2023.0.x (No Train Change)

### pom.xml Changes
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.12</version>
    <relativePath/>
</parent>

<!-- Spring Cloud BOM stays on 2023.0.x — Spring Boot 3.3.x is supported by this train -->
<properties>
    <spring-cloud.version>2023.0.6</spring-cloud.version>
</properties>
```

### 2.1 Spring Boot Breaking Changes in This Phase

#### CDS (Class Data Sharing) — Awareness Only
- Spring Boot 3.3 adds CDS support via `spring.context.checkpoint`.
- No action required unless you are building optimised JVM images or AOT-compiled binaries.
- If using `spring-boot-maven-plugin` for OCI image builds, review builder defaults —
  the default CNB builder changed. Verify your `spring-boot:build-image` output is as expected.

#### Docker Compose / Testcontainers — ConnectionDetailsFactory API
- If using Spring Boot's Docker Compose integration or Testcontainers connection details
  auto-configuration, the `ConnectionDetailsFactory` API is updated.
- Review any custom `ConnectionDetailsFactory` implementations for API signature changes.

#### Actuator Endpoint Access Model — Preparation for 3.5 Tightening
- Begin auditing `management.endpoints.web.exposure.include` across all profiles.
- Avoid wildcard `*` in production profiles. Enumerate only the endpoints you intentionally expose.
- This is preparation — the `heapdump` access default change arrives in Phase 4.

#### Logging Framework Updates
- Logback and Log4j2 version bumps. If you have custom `logback-spring.xml` or
  `log4j2-spring.xml` configurations, verify they parse cleanly under the updated versions.
- Look for deprecation warnings at application startup in the logging configuration phase.

### 2.2 Spring Cloud in This Phase
- Spring Cloud 2023.0.x supports both Spring Boot 3.2.x and 3.3.x.
- No BOM version change is required.
- Verify all Spring Cloud components function correctly under Boot 3.3.x.
- If you use Spring Cloud Contract for consumer-driven contract tests, verify test generation
  is compatible with the updated Spring Boot test slice configurations.

### Phase 2 Validation Checklist
```
[ ] ./mvnw clean verify — zero compilation errors, zero test failures
[ ] Verify structured logging output format is unchanged on all profiles
[ ] Testcontainers-based integration tests pass
[ ] Config Server / Config Client connectivity verified
[ ] All OpenFeign client tests pass
```

---

## PHASE 3 — Spring Boot 3.3.x → 3.4.x | Spring Cloud 2023.0.x → 2024.0.x
> ⚠️ HIGH RISK — Most breaking changes are concentrated in this phase.
> Resolve ALL items before proceeding to Phase 4.

### pom.xml Changes
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.6</version>
    <relativePath/>
</parent>

<!-- Spring Cloud BOM — MUST upgrade to 2024.0.x for Spring Boot 3.4.x -->
<properties>
    <spring-cloud.version>2024.0.3</spring-cloud.version>
</properties>
```

### 3.1 Spring Boot Breaking Changes in This Phase

#### CRITICAL — Removed Deprecated APIs
All classes, methods, and properties deprecated in Spring Boot **3.2.x** and marked for
removal in 3.4 are **permanently removed**. This is the primary cause of compilation failures.

Fix every deprecated usage identified in Phase 0 before bumping to 3.4.x.
Common Spring Boot 3.2 deprecations now removed:
- Various `SpringApplication` configuration methods replaced by `SpringApplicationBuilder`
- Actuator `HealthContributor` adapters refactored — old adapters removed
- Certain auto-configuration condition APIs replaced in Spring Framework 6.1

#### CRITICAL — @ConfigurationProperties + @Valid Cascade Change
Prior behaviour: Nested `@ConfigurationProperties` validation fired automatically, even
without `@Valid` on the field.
New behaviour: Validation ONLY cascades to nested properties if the field is annotated `@Valid`.

Action: Audit every `@ConfigurationProperties` class annotated with `@Validated`.
Add `@Valid` on EVERY field that references a nested properties class.

```java
// ❌ BEFORE — nested validation fired accidentally (3.3 and earlier)
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {
    private ServiceProperties service;
    private DatabaseProperties database;
}

// ✅ AFTER — @Valid required explicitly on each nested field (3.4+)
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @Valid
    private ServiceProperties service;

    @Valid
    private DatabaseProperties database;
}
```
Missing `@Valid` means nested Bean Validation constraints silently stop being evaluated.
This is a runtime regression that will NOT produce a compilation error.

#### HTTP Client Redirect Behaviour Changed
Prior behaviour: RestClient, RestTemplate, WebClient do NOT follow redirects by default.
New behaviour: ALL five auto-configured HTTP clients now follow redirects by default.

Action: Audit all outbound HTTP calls. If external services return intentional 3xx responses
(OAuth2, legacy redirect-based APIs), verify the new follow-redirect behaviour is correct.
To explicitly disable:
```yaml
spring:
  http:
    client:
      redirects: dont-follow
```

#### @ConditionalOnBean / @ConditionalOnMissingBean — Generics Considered
New behaviour: Generic type parameters are now considered when matching `@Bean` method return types.
If you have custom auto-configurations or conditional beans, verify existing beans still
match or are excluded correctly.

#### HtmlUnit Dependency Coordinates Changed (Test Scope Only)
```xml
<!-- ❌ OLD -->
<dependency>
    <groupId>net.sourceforge.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <scope>test</scope>
</dependency>

<!-- ✅ NEW -->
<dependency>
    <groupId>org.htmlunit</groupId>
    <artifactId>htmlunit</artifactId>
    <scope>test</scope>
</dependency>
```
Package names also changed: `com.gargoylesoftware.htmlunit` → `org.htmlunit`.

#### Structured Logging (New — Optional)
Spring Boot 3.4 introduces structured logging (ECS, GELF, Logstash formats).
No mandatory action. To enable structured JSON console logging:
```yaml
logging:
  structured:
    format:
      console: logstash
      file: logstash
```

### 3.2 Spring Cloud Breaking Changes in This Phase (2023.0.x → 2024.0.x)

#### Spring Cloud Gateway — X-Forwarded-* Headers NOT Disabled Yet (2024.0.x)
- The full X-Forwarded-* header disable arrives in **2025.0.x** (Phase 4).
- In 2024.0.x, verify your gateway route configuration for any deprecated filter/predicate names.

#### OpenFeign — API Alignment with Spring Cloud 2024.0.x
- OpenFeign client interface proxying behaviour is updated to align with the new
  Spring Framework 6.1 HTTP interface model.
- If you have `@FeignClient` interfaces that use `@RequestMapping` alongside
  the newer `@HttpExchange`, verify they compile and function correctly.
- Ensure all OpenFeign error decoders still handle HTTP responses correctly.

#### Spring Cloud Config — Property Source Priority
- Config Server property source ordering has been updated. If you override Config Server
  values with local properties, verify the precedence is still correct.
  Priority: Local profile properties > Config Server > application.yml defaults.

#### Resilience4j — Metrics and Observability Integration
- In Spring Cloud 2024.0.x, Resilience4j uses `ObservationRegistry` natively.
- If you have custom `CircuitBreakerMetricsPublisher` beans or Resilience4j metric
  endpoint configurations, they may conflict with the updated auto-configuration.

#### Eureka Client — Updated Registration Model
- Eureka client registration uses the updated `EurekaInstanceConfigBean`.
- Verify metadata map entries and custom `EurekaInstanceConfigBean` customisers still apply.
- If you use instance metadata for routing (`eureka.instance.metadata-map`), test routing is correct.

### Phase 3 Validation Checklist
```
[ ] ./mvnw clean verify — IF compilation fails, root cause is removed deprecated APIs, fix them first
[ ] @ConfigurationProperties validation verified end-to-end for all nested property classes
[ ] External HTTP call behaviour verified — correct redirect handling
[ ] Spring Cloud Gateway routes verified — all route predicates and filters function correctly
[ ] Eureka registration/deregistration verified
[ ] Config Server property loading verified on all profiles
[ ] OpenFeign clients verified — all inter-service calls function correctly
[ ] Resilience4j circuit breaker trip/recovery verified
[ ] All integration tests pass
```

---

## PHASE 4 — Spring Boot 3.4.x → 3.5.13 | Spring Cloud 2024.0.x → 2025.0.x
> ⚠️ HIGH RISK — Security defaults, bean naming, profile rules, and Spring Cloud Gateway
> all change significantly in this phase.

### pom.xml Changes
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.13</version>
    <relativePath/>
</parent>

<!-- Spring Cloud BOM — MUST upgrade to 2025.0.x for Spring Boot 3.5.x -->
<properties>
    <spring-cloud.version>2025.0.1</spring-cloud.version>
    <java.version>17</java.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 4.1 Spring Boot Breaking Changes in This Phase

#### CRITICAL — `taskExecutor` Bean Name Removed
Prior behaviour: Auto-configured `TaskExecutor` was registered under BOTH
`taskExecutor` AND `applicationTaskExecutor` bean names.
New behaviour: ONLY `applicationTaskExecutor` is registered.

Action: Search the entire codebase using the following patterns and update ALL occurrences:
```java
// ❌ Pattern 1 — @Qualifier injection
@Autowired
@Qualifier("taskExecutor")
private TaskExecutor executor;
// ✅ Fix
@Qualifier("applicationTaskExecutor")

// ❌ Pattern 2 — @Resource by name
@Resource(name = "taskExecutor")
private Executor executor;
// ✅ Fix
@Resource(name = "applicationTaskExecutor")

// ❌ Pattern 3 — ApplicationContext.getBean by name
context.getBean("taskExecutor", TaskExecutor.class);
// ✅ Fix
context.getBean("applicationTaskExecutor", TaskExecutor.class);

// ❌ Pattern 4 — @Async executor specification
@Async("taskExecutor")
public CompletableFuture<Void> asyncMethod() { ... }
// ✅ Fix
@Async("applicationTaskExecutor")
```
> Note: If you defined your OWN `taskExecutor` bean (not relying on auto-configuration),
> this change does NOT affect your custom bean — only the auto-configured one is renamed.

#### CRITICAL — `heapdump` Actuator Endpoint Defaults to `access=NONE`
Prior behaviour: Exposing `/actuator/heapdump` in `exposure.include` was sufficient.
New behaviour: Access defaults to `NONE`. You must explicitly configure it.

Action: If you expose the heapdump endpoint, update management config:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, loggers, heapdump
  endpoint:
    heapdump:
      access: unrestricted   # Must be explicitly set — no longer implicit
```
If you do NOT use heapdump, no action needed. The new default is a security improvement.

#### CRITICAL — `.enabled` Property Values Strictly `true` or `false`
Prior behaviour: Some `.enabled` properties accepted `yes`, `1`, `on`, etc.
New behaviour: ONLY the exact strings `true` or `false` are accepted at binding time.
Anything else causes a `BindException` and application startup failure.

Action: Search all property files for `enabled:` or `enabled=` and verify each value:
```yaml
# ❌ INVALID — startup fails with BindException
feature.x.enabled: yes
feature.x.enabled: 1
feature.x.enabled: on
feature.x.enabled: TRUE     # Verify case — must be lowercase

# ✅ VALID
feature.x.enabled: true
feature.x.enabled: false
```

#### CRITICAL — Profile Naming Rules Strictly Enforced
Prior behaviour: Profile names with spaces, leading/trailing dashes were silently accepted.
New behaviour: Profiles can ONLY contain letters, digits, `-` (dash), and `_` (underscore).
Cannot start or end with `-` or `_`. Violation causes startup failure.

Action: Audit all profile name usages:
```yaml
# ❌ INVALID
spring.profiles.active: " local"    # leading space
spring.profiles.active: "local "    # trailing space
spring.profiles.active: "-local"    # starts with dash
spring.profiles.active: "local_"    # ends with underscore

# ✅ VALID
spring.profiles.active: local
spring.profiles.active: docker-prod
spring.profiles.active: local_dev
```
Also check `@Profile("...")` annotations in Java code, JVM args
(`-Dspring.profiles.active=...`), and `SPRING_PROFILES_ACTIVE` environment variables
in Docker Compose files and Kubernetes manifests.

#### CRITICAL — `spring.data.redis.database` Ignored When URL Is Configured
Prior behaviour: `spring.data.redis.database` applied even when `spring.data.redis.url` was set.
New behaviour: When URL is configured, database is determined by the URL path segment only.
`spring.data.redis.database` is silently ignored.

Action: Embed the database index directly in the URL:
```yaml
# ❌ BEFORE — database=1 was applied on top of url (no longer works)
spring:
  data:
    redis:
      url: redis://redis-host:6379
      database: 1

# ✅ AFTER — embed database index as path segment in URL
spring:
  data:
    redis:
      url: redis://redis-host:6379/1
      # Remove the standalone 'database' property
```

#### `taskExecutor` Background Bean Initialisation (New Default Behaviour)
Spring Boot 3.5 auto-configures a `bootstrapExecutor` bean if `applicationTaskExecutor`
exists in the context, enabling bean background initialisation by default.
Action: Verify that beans with strict initialisation ordering are not affected.
Add `@DependsOn` annotations where a bean must be fully initialised before another:
```java
@Bean
@DependsOn("criticalInfrastructureBean")
public MyService myService() { ... }
```

#### WebClient Redirect Behaviour Changed
Spring Boot 3.5 aligns WebClient with blocking client defaults from 3.4.
WebClient now follows redirects by default.
Action: If your microservice uses `WebClient` and must NOT follow redirects, configure:
```yaml
spring:
  http:
    client:
      redirects: dont-follow
```

#### `spring-boot-parent` Module Removed
The internal `spring-boot-parent` module is no longer published to Maven Central.
Action: If your Maven configuration references `spring-boot-parent` as a dependency
(not `spring-boot-starter-parent`), replace it with your own `<dependencyManagement>` block.
Most applications are not affected — verify and skip if not applicable.

#### Major Third-Party Dependency Version Jumps
The Spring Boot 3.5.13 BOM introduces significant version changes. Verify each:

| Dependency    | 3.1.x Version | 3.5.13 BOM    | Action                                               |
|---------------|---------------|----------------|------------------------------------------------------|
| Flyway        | 9.x           | 11.7.x         | Review migration scripts; Flyway 10+ dropped legacy DB support |
| Hibernate     | 6.2.x         | 6.4.x          | Review HQL/JPQL queries; ID generator strategy changes|
| Kafka Client  | 3.4.x         | 3.9.x          | Review deserializer error handling; producer config  |
| Jackson       | 2.15.x        | 2.19.x         | Review custom Module/ObjectMapper configuration      |
| Liquibase     | 4.20.x        | 4.31.x         | Two new properties added (see 4.2 below)             |
| Micrometer    | 1.11.x        | 1.15.x         | Review MeterRegistry customisations                  |
| OpenTelemetry | 1.28.x        | 1.49.x         | Review span/trace attribute names                    |

**Flyway-specific action** — jumping from Flyway 9 to Flyway 11 is a MAJOR version jump:
- Flyway 10 changed the locking behaviour on `flyway_schema_history`.
- Flyway 11 requires callbacks and Java-based migrations to implement new interfaces.
- If using Flyway Teams/Enterprise features, a license key is now required via config.
- Override the BOM version only if a specific Flyway 11.x version is incompatible:
  ```xml
  <properties>
      <flyway.version>11.7.0</flyway.version>
  </properties>
  ```

**Liquibase-specific action** — two new properties added in 3.5.13:
```yaml
spring:
  liquibase:
    analytics-enabled: false     # Disable product usage telemetry (recommended for production)
    license-key:                 # Only needed if using Liquibase Pro
```

### 4.2 Spring Cloud Breaking Changes in This Phase (2024.0.x → 2025.0.x)

#### CRITICAL — Spring Cloud Gateway: X-Forwarded-* Headers DISABLED by Default
Prior behaviour: Spring Cloud Gateway forwarded `X-Forwarded-Host`, `X-Forwarded-Port`,
`X-Forwarded-Proto`, `X-Forwarded-Prefix`, and `Forwarded` headers to downstream services by default.
New behaviour: These headers are **DISABLED by default** as a security measure to prevent
header spoofing attacks.

Action: If your downstream microservices rely on these headers (e.g., for base URL construction,
HTTPS detection, or redirect building), you MUST configure trusted proxies:

```yaml
# For Spring Cloud Gateway Server (WebFlux — the standard reactive gateway)
spring:
  cloud:
    gateway:
      server:
        webflux:
          trusted-proxies: "10\\.0\\.0\\..*"   # Java regex matching your proxy IP range

# For Spring Cloud Gateway Server MVC (WebMVC style — available from 4.1.x onward)
spring:
  cloud:
    gateway:
      mvc:
        trusted-proxies: "10\\.0\\.0\\..*"
```

Additionally, audit every downstream service that uses `HttpServletRequest.getScheme()`,
`request.getServerName()`, `UriComponentsBuilder.fromCurrentRequest()`, or any Spring
`ForwardedHeaderFilter`-based URL reconstruction — these all depend on forwarded headers.

#### Spring Cloud Gateway — Module and Starter Name Changes (2025.0.x)
Spring Cloud 2025.0.0 introduces new starter names to clarify the two gateway styles.
Old starter names are deprecated and will be removed in the next major version.

```xml
<!-- ❌ DEPRECATED artifact names (still work but log deprecation warnings) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- ✅ NEW artifact names — choose the one matching your web stack -->
<!-- Reactive gateway (WebFlux) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>

<!-- MVC-style gateway (WebMVC — Proxy Exchange) -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-mvc</artifactId>
</dependency>
```

Update your `pom.xml` to use the new artifact names. The old names still resolve
but generate deprecation warnings.

Also: the `WebClientRouting` infrastructure inside Spring Cloud Gateway is deprecated
in 2025.0.x and will be removed in 5.0. Begin planning migration away from it.

#### Spring Cloud Gateway — Deprecated Property Prefixes
Spring Cloud 2025.0.x introduces new property prefixes for gateway configuration.
Use `spring-boot-properties-migrator` at startup to detect all deprecated gateway
property keys. The old prefixes are still bound but log warnings.

Common prefix changes:
```yaml
# ❌ OLD prefix (deprecated)
spring.cloud.gateway.routes[0].filters

# ✅ NEW prefix (use this)
spring.cloud.gateway.server.webflux.routes[0].filters
```

#### Spring Cloud — Jackson 2.19.x Mandatory
Spring Boot 3.5.x brings Jackson 2.19.x. Spring Cloud 2025.0.0 was specifically released
to be compatible with this version.
Action: If you have pinned Jackson to an older version in `<properties>`, remove the override
and let the BOM manage it. Verify no custom `Jackson2ObjectMapperBuilderCustomizer` breaks
with Jackson 2.19.x.

#### Spring Cloud Kubernetes — Fabric8 7.3.1 Upgrade
If your microservice uses `spring-cloud-kubernetes`:
- Spring Cloud 2025.0.x upgrades Fabric8 Kubernetes Client from 6.x to 7.3.1.
- This is a MAJOR version jump in Fabric8 with API changes.
- Review any direct usage of `io.fabric8.kubernetes.client.*` APIs.
- Verify that KubernetesClient bean injection still works with the new version.

#### spring-cloud-starter-parent Deprecated
If your project uses `spring-cloud-starter-parent` as a Maven parent, be aware:
this artifact is deprecated in Spring Cloud 2025.0.x and will be removed in the
next major release. Plan to migrate to `spring-boot-starter-parent` + BOM import.

#### Eureka Client — Health Check and Metadata
- Verify custom `EurekaInstanceConfigBean` customisers still apply with updated auto-config.
- Test service registration, heartbeat, and deregistration under 2025.0.x.
- If using Eureka health check handler integration, verify it cooperates correctly with
  the updated Spring Boot 3.5.x actuator health model.

#### OpenFeign — Observability Alignment
- OpenFeign is fully aligned with Spring Boot 3.5.x Micrometer / OpenTelemetry integration.
- If you have custom `FeignClientFactory` or `Targeter` beans, verify they work with
  the updated auto-configuration.

### Phase 4 Validation Checklist
```
[ ] ./mvnw clean verify — zero compilation errors, zero test failures
[ ] Application starts cleanly on all profiles — ZERO startup errors in logs
[ ] No BindException on startup (check .enabled properties, profile names)
[ ] taskExecutor → applicationTaskExecutor: all async methods work correctly
[ ] heapdump endpoint: if exposed, access=unrestricted is set and endpoint responds
[ ] Redis: correct database is used — verify via Redis MONITOR or integration test
[ ] Flyway: migrations run cleanly on a clean schema — verify all V*.sql scripts execute
[ ] Kafka: producer sends and consumer receives messages end-to-end
[ ] Spring Cloud Gateway: verify all routes proxy correctly to downstream services
[ ] Spring Cloud Gateway: X-Forwarded-* headers — verify downstream services reconstruct
    URLs correctly, or confirm they do not rely on forwarded headers
[ ] Spring Cloud Gateway: new starter artifact names updated in pom.xml
[ ] Eureka: service registers, heartbeats, deregisters correctly
[ ] Config Server: all profile-specific config values are loaded correctly
[ ] OpenFeign: all inter-service calls succeed with correct request/response handling
[ ] Resilience4j: circuit breaker opens, half-opens, and closes on threshold correctly
[ ] Actuator: /health UP, /info correct, /metrics populated, /loggers responds
[ ] No deprecated property warnings in logs (properties migrator output)
[ ] Integration and contract tests fully green
```

---

## PHASE 5 — POST-UPGRADE TASKS

### 5.1 Remove the Properties Migrator
```xml
<!-- REMOVE this dependency — it must not be in the final production build -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-properties-migrator</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 5.2 Resolve All TODO Comments from Phase 1
- Review all `// TODO: Migrate to RestClient` comments added in Phase 1.
- Schedule a follow-up ticket to migrate `RestTemplate` usages to `RestClient`
  (Spring Boot 3.5.x fully supports `RestClient` with auto-configuration and connection timeouts).

### 5.3 Address New Deprecations Introduced in 3.5.x
Run the deprecation scan on the final 3.5.13 build:
```bash
./mvnw clean compile -Xlint:deprecation 2>&1 | grep "warning.*deprecated" | sort | uniq
```
Document new deprecations for the next upgrade cycle.

### 5.4 Upgrade to Java 21 (Strongly Recommended)
Spring Boot 3.5.13 fully supports Java 17, 21, and 24.
Java 21 (LTS) provides Virtual Threads (stable), Sequenced Collections, record patterns,
and significant JVM GC improvements. Recommended upgrade path:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

After upgrading to Java 21, enable Virtual Threads:
```yaml
spring:
  threads:
    virtual:
      enabled: true
```
Test all `@Async` methods, scheduled tasks, Kafka consumers, and Eureka background threads
with Virtual Threads enabled before deploying to production.

### 5.5 Update Spring Cloud Gateway Starter Artifact Names
If not done in Phase 4, update gateway starter artifacts to the new names:
```xml
<!-- ✅ Reactive gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
```

### 5.6 Update Docker Base Images
Update all Dockerfiles to use a JDK 21 base image:
```dockerfile
# Recommended base images
FROM eclipse-temurin:21-jre-jammy
# or minimal
FROM eclipse-temurin:21-jre-alpine
```

### 5.7 Final End-to-End Verification Checklist
```
[ ] ./mvnw clean verify — 100% green build
[ ] spring-boot-properties-migrator REMOVED from pom.xml
[ ] No deprecated property warnings in startup logs on any profile
[ ] No deprecated API warnings from -Xlint:deprecation
[ ] ./mvnw spring-boot:run -Dspring-boot.run.profiles=local — clean startup, zero errors
[ ] ./mvnw spring-boot:run -Dspring-boot.run.profiles=docker — clean startup, zero errors
[ ] Actuator /health: status=UP, all components UP
[ ] Actuator /info: correct build info returned
[ ] Actuator /metrics: micrometer metrics populated
[ ] Actuator /loggers: responds, log level changes work at runtime
[ ] Eureka: service visible in Eureka dashboard, heartbeats firing
[ ] Config Server: all environment-specific values loaded correctly
[ ] Spring Cloud Gateway: all routes proxied, filters applied, rate limits enforced
[ ] Spring Cloud Gateway: X-Forwarded-* header behaviour confirmed correct end-to-end
[ ] OpenFeign: all inter-service calls succeed, timeouts enforced, retries work
[ ] Resilience4j: circuit breaker state transitions verified in integration test
[ ] Kafka: producer → topic → consumer roundtrip verified
[ ] Database migrations: Flyway / Liquibase run cleanly on a fresh schema
[ ] Redis: correct database index used, TTL and eviction policies verified
[ ] OAuth2/JWT security: all secured endpoints return correct 200/401/403 responses
[ ] Async methods: @Async with applicationTaskExecutor functions correctly
[ ] Scheduled tasks: @Scheduled methods fire at correct intervals
[ ] Docker image builds successfully: ./mvnw spring-boot:build-image
[ ] Load/smoke test run against staging — no regressions in latency or throughput
```

---

## QUICK REFERENCE — Most Common Failure Modes After This Upgrade

| Symptom at Startup / Runtime                          | Root Cause                              | Fix                                                  |
|-------------------------------------------------------|-----------------------------------------|------------------------------------------------------|
| `NoSuchBeanDefinitionException: taskExecutor`         | 4.1 — bean name removed                 | Change to `applicationTaskExecutor`                  |
| `403 Forbidden` on `/actuator/heapdump`               | 4.1 — access defaults to NONE           | Set `endpoint.heapdump.access=unrestricted`          |
| `BindException` on startup for `.enabled` property    | 4.3 — strict boolean enforcement        | Set value to exactly `true` or `false`               |
| `IllegalArgumentException: invalid profile name`      | 4.4 — profile naming enforced           | Remove spaces, leading/trailing dash or underscore   |
| Wrong Redis database being used                       | 4.5 — `database` ignored with URL       | Embed database index as `/N` in URL                  |
| `CompilationFailure` after Phase 3 bump               | 3.1 — removed deprecated APIs           | Fix all 3.2 deprecations before Phase 3 starts       |
| Nested `@ConfigurationProperties` validation silent   | 3.2 — `@Valid` not cascading            | Add `@Valid` on nested property fields               |
| External API calls fail / unexpected redirects        | 3.3/4.6 — redirect defaults changed     | Set `spring.http.client.redirects=dont-follow`       |
| Flyway migration fails at startup                     | 4.8 — Flyway 11.x major version jump    | Review migration scripts; update callback interfaces |
| Downstream services get wrong scheme/host             | Spring Cloud 4.2 — forwarded headers disabled | Configure `trusted-proxies` in gateway config   |
| `Application startup veto` on Spring Cloud startup    | Spring Cloud / Boot version mismatch    | Align BOM train — Boot 3.5.x needs Cloud 2025.0.x   |
| Feign client `NoSuchMethodError` at runtime           | Spring Cloud train mismatch             | Ensure 2025.0.x BOM is active, not a cached 2023.0.x |
| Jackson `MismatchedInputException` in Cloud routes    | 4.2 — Jackson 2.19.x compatibility     | Remove pinned Jackson version; let BOM manage it     |
| Gateway route `404` after upgrade                     | 4.2 — deprecated gateway property prefix | Migrate to new `server.webflux.*` property prefix   |
