# Project Conventions

This document is the single source of truth for how code in this codebase is
structured and written. It exists primarily so that AI agents (and humans)
working on this project make the same decisions every time, instead of each
session inventing its own pattern.

**Rule zero:** if a situation isn't covered here, find the nearest existing
pattern in the codebase and match it. Do not introduce a new pattern to solve
a problem this doc doesn't mention. If you hit a real gap, flag it instead of
silently improvising — add a TODO comment referencing this file and surface
it to a human reviewer.

**Rule zero-point-five:** if you find two competing patterns already in the
codebase for the same kind of problem, do not preserve both "for
compatibility." Pick the one that matches this doc, migrate the other, and
treat the leftover as tech debt — not as a second valid convention.

---

## 0. Tech Stack

| Layer | Technology | Notes |
|-------|-----------|-------|
| Language | Java 17 | Source & target |
| Framework | Spring Boot 3.5 | Parent POM |
| Web layer | Spring MVC + Thymeleaf | Server-rendered templates, no REST API |
| Data access | Spring Data JPA + Hibernate 6 | JPA repositories |
| Database | PostgreSQL | via `org.postgresql:postgresql` (runtime scope) |
| Validation | Hibernate Validator 8 | via `spring-boot-starter-validation` |
| Security | Spring Security + Argon2 | `spring-security-crypto` via `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()` |
| Email | Spring Mail | `spring-boot-starter-mail` |
| Image processing | WebP imageio (0.1.6) | Image compression to WebP format |
| Cryptography | Bouncy Castle (bcprov-jdk18on 1.78.1) | Transitive dependency of Spring Security's Argon2 — not imported directly in application code |
| Build tool | Maven 3.9 | No Gradle |
| Code generation | Lombok | `@Getter @Setter`, `@RequiredArgsConstructor`, `@Slf4j` |
| Frontend | Alpine.js 3 | CDN-loaded, used in staff-facing pages |
| Fonts | Cormorant Garamond + DM Sans | Google Fonts |
| Logging | SLF4J + Logback | Via Spring Boot starter |
| Testing | JUnit 5 + Mockito | Via `spring-boot-starter-test` |
| Dev tools | Spring DevTools | Runtime scope |

---

## 1. Feature Module Anatomy

Every feature lives under `features/{feature}/` with this shape:

```
features/{feature}/
├── controller/      — @Controller, @RequestMapping prefix          [required]
├── service/         — @Service, business logic                     [required]
├── entity/          — @Entity, JPA mappings                        [required]
├── dto/             — request *Form records + response *View records [required]
├── repository/      — Spring Data JPA interfaces                   [required]
├── mapper/          — static utility classes                       [optional]
├── validation/      — @Component validators                        [optional]
└── enums/           — enum types                                   [optional]
```

- `mapper/` is optional only if mapping logic is trivial enough to live as a
  `*View.from(entity)` static factory on the DTO itself (see §5). Once a
  mapping needs more than ~2 source entities or any computed/derived field,
  it graduates to a dedicated mapper class.
- `validation/` is optional only if the feature has no validation beyond
  what Jakarta Validation annotations express declaratively on the DTO.
- `enums/` is optional if the feature has no enum types of its own (it may
  still reference enums from another feature).
- Do not invent new top-level sub-packages (e.g. `util/`, `helper/`) inside a
  feature. If something doesn't fit the categories above, it probably
  belongs in `service/` or in a shared cross-cutting package (§9).

**Anti-pattern:** a `features/reservation/util/ReservationHelpers.java`
grab-bag class. If it's a pure function operating on DTOs/entities, it's a
mapper. If it has business rules, it's a service method.

---

## 2. Controller Conventions

- `@Controller` (not `@RestController` — this app renders server-side
  templates) + `@RequestMapping("/prefix")` at the class level.
- `@RequiredArgsConstructor` for constructor injection — dependencies are
  `private final` fields. No `@Autowired`, no field injection, no manual
  constructors. Manual constructors are tech debt; migrate them on touch.
- A class-level `@ModelAttribute void addCommonAttributes(Model model)`
  method sets `Attributes.ACTIVE_SIDEBAR` for the feature. **Staff-only
  convention** — guest-facing controllers (`/`, `/rooms`, `/book`,
  `/my-booking/**`, `/contact`, `/policies`) use a header navbar layout
  (`guest/layout.html`) instead of a sidebar, so they do not set
  `ACTIVE_SIDEBAR` or include an `addCommonAttributes` method.
  Guest controllers that need navigation highlighting add an `activePage`
  model attribute implicitly via the handler or explicitly before returning
  the view name.
- Form binding: `@Valid @ModelAttribute(Attributes.FORM) SomeForm form,
  BindingResult binding`. Custom validators (§6) run *after* JSR-380
  annotation validation and add to the same `BindingResult`.
- On validation failure, **re-render the same view** with the model
  re-populated — do not redirect. Validation errors must survive on the
  current page; flash attributes are for *successful or failed actions*,
  not form re-display.
- On successful POST, **redirect** (never return a view name directly after
  a mutation — avoids duplicate-submit-on-refresh) and set exactly one flash
  attribute: `Attributes.SUCCESS` or `Attributes.ERROR`. Never both, never
  neither.
- **Ownership of nested resources is never assumed from path variables
  alone.** A route like `/{id}/manage/guests/{guestId}/delete` must have the
  service verify that `guestId` actually belongs to reservation `id` before
  acting — see §3. The controller's job is to pass both IDs through, not to
  check the relationship itself, and definitely not to skip the check.
- Business-rule failures from a POST handler are caught at the controller
  only if you intend to render a flash message in the same method. Prefer
  the global exception mapping in §9 over local try/catch where possible;
  local try/catch is acceptable when the success path also needs a flash
  message in the same redirect (see the `assignRoom` example below).
- Resource routes use `@PathVariable Long id` (not `String`, not custom
  path-variable types).
- When a view needs more than ~3 related attributes, or the same attribute
  set is assembled in more than one handler (e.g. a GET and its
  error-recovery POST), introduce a single `*View` record (§5) and bind it
  under `Attributes.VIEW`. Simple 1–2 attribute pages may use
  `model.addAttribute(...)` calls directly — don't force a DTO on a
  three-line method.
- Build redirect URLs with `UriComponentsBuilder` (for query params) or
  plain path concatenation only for single, fixed, ID-only segments. Never
  concatenate more than one variable into a path string by hand.

**Canonical form:**
```java
@Controller
@RequestMapping("/manager/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomValidator roomValidator;

    @ModelAttribute
    void addCommonAttributes(Model model) {
        model.addAttribute(Attributes.ACTIVE_SIDEBAR, "ROOMS");
    }

    @GetMapping
    String list(...) { ... }

    @PostMapping
    String create(@Valid @ModelAttribute(Attributes.FORM) RoomCreateForm form,
                  BindingResult binding,
                  RedirectAttributes redirect) {
        roomValidator.validate(form, binding);
        if (binding.hasErrors()) {
            return "manager/room/create";
        }
        roomService.createRoom(form);
        redirect.addFlashAttribute(Attributes.SUCCESS, "Room created.");
        return "redirect:/manager/rooms";
    }
}
```

**Anti-pattern — silent catch:**
```java
try {
    roomAssignmentService.assignRoom(id, roomId);
} catch (IllegalStateException e) {
    // ignore duplicate or already-occupied
}
```
**Correct:**
```java
try {
    roomAssignmentService.assignRoom(id, roomId);
    redirect.addFlashAttribute(Attributes.SUCCESS, "Room assigned.");
} catch (IllegalStateException e) {
    redirect.addFlashAttribute(Attributes.ERROR, e.getMessage());
}
```

**Anti-pattern — trusting unrelated path variables:**
```java
@PostMapping("/{id}/manage/guests/{guestId}/delete")
String deleteGuest(@PathVariable Long id, @PathVariable Long guestId) {
    stayingGuestService.deleteGuest(guestId); // never checks guestId ∈ reservation id
    ...
}
```
**Correct:**
```java
stayingGuestService.deleteGuest(id, guestId); // service verifies the relationship
```

**Anti-pattern — raw multi-segment redirect concatenation:**
```java
return "redirect:/receptionist/reservations/create/details"
        + "?checkInAt=" + form.getCheckInAt() + "&checkOutAt=" + form.getCheckOutAt();
```
**Correct:**
```java
return "redirect:" + UriComponentsBuilder
        .fromPath("/receptionist/reservations/create/details")
        .queryParam("checkInAt", form.getCheckInAt())
        .queryParam("checkOutAt", form.getCheckOutAt())
        .toUriString();
```

---

## 3. Service Conventions

- `@Service` + `@RequiredArgsConstructor` — same rule as controllers: no
  field injection, no manual constructors.
- `@Transactional` annotation: **always**
  `org.springframework.transaction.annotation.Transactional`. The JTA
  variant (`jakarta.transaction.Transactional`) is **not used** anywhere in
  new code. If you find it on an existing method, that's tech debt — migrate
  it to the Spring annotation as part of any change you make to that method,
  don't add more JTA usages.
- Apply `@Transactional` on **write methods** (create/update/delete).
  Read-only query methods do not need it unless they span multiple
  repository calls that must be consistent.
- `findById(id)` (or equivalent single-resource lookup) **always** throws
  `ResourceNotFoundException` if missing — never returns `null`, never
  returns `Optional` to the controller layer. `Optional` is fine as an
  internal implementation detail inside the service, but the public service
  method signature returns the entity or throws.
- Business rule violations (e.g. "room already occupied", "cannot extend a
  checked-out reservation") throw `IllegalStateException` with a
  human-readable message — that message is what ends up in the flash
  attribute, so write it for the receptionist, not for a stack trace.
- Invalid input that isn't caught by `@Valid` (e.g. a numeric argument that's
  syntactically fine but semantically wrong, like `extraDays <= 0`) throws
  `IllegalArgumentException`, same message rule as above.
- **Any method touching a child resource scoped to a parent must accept
  both IDs and verify the relationship before acting.** This is the
  authoritative fix for the ownership gap described in §2:

```java
@Transactional
public void deleteGuest(Long reservationId, Long guestId) {
    var guest = stayingGuestRepository.findById(guestId)
            .orElseThrow(() -> new ResourceNotFoundException("Guest not found"));
    if (!guest.getReservation().getId().equals(reservationId)) {
        throw new IllegalArgumentException("Guest does not belong to this reservation");
    }
    stayingGuestRepository.delete(guest);
}
```

- Magic numbers belong in named `static final` constants on the service (or
  in `HotelConstants`, §9, if shared across features) — never inline. E.g.
  image max dimensions, max guests per room, etc.

**Anti-pattern:**
```java
public StayingGuest getGuest(Long guestId) {
    return repository.findById(guestId).orElse(null); // controller has to null-check
}
```

---

## 4. Entity Conventions

- `@Entity` + `@Table(name = "snake_case_plural")`.
- Lombok `@Getter @Setter` — **not** `@Data`. `@Data` generates
  `equals`/`hashCode`/`toString` based on all fields, which is dangerous on
  JPA entities (lazy-loading triggers, circular references, mutable
  identity). If you need `equals`/`hashCode`, write them explicitly based on
  `id` only.
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- `@Column(name = "snake_case")` explicitly on every column — don't rely on
  Hibernate's naming strategy inferring it correctly.
- Enums map with `@Enumerated(EnumType.STRING)`. Never `EnumType.ORDINAL` —
  it silently corrupts data if enum order changes.
- Associations default to `FetchType.LAZY`. `EAGER` requires a comment
  explaining why (e.g. "always needed for X computation, avoids N+1 in every
  caller").
- Timestamps (`createdAt`, `updatedAt`) are set via `@PrePersist` /
  `@PreUpdate` lifecycle callbacks on the entity itself, not in service code.
- Soft delete is implemented via a `boolean active` column, **default
  `true`**. Any entity using soft delete must either:
  - have `@Where(clause = "active = true")` on the entity class, so all
    standard repository finders exclude inactive rows by default, **or**
  - if `@Where` isn't used (e.g. because an admin screen needs to see
    inactive rows), every custom repository query must explicitly filter on
    `active = true` and this must be called out in a comment on the query
    method. Pick one approach per entity and document which one in a
    comment above the `active` field — don't mix both within the same
    entity.

**Anti-pattern:**
```java
@Data
@Entity
public class Reservation { ... } // generates equals/hashCode over all fields incl. lazy associations
```

---

## 5. DTO / View Model Conventions

- **Request DTOs** are `*Form` records: immutable, carry Jakarta Validation
  annotations (`@NotNull`, `@Size`, etc.) directly on the fields. Bound via
  `@ModelAttribute(Attributes.FORM)`.
- **Response/view DTOs** are `*View` records with a `static *View
  from(Entity entity)` factory method for simple 1:1 mappings. If the
  mapping needs more than one source entity, a repository lookup, or
  non-trivial computed fields, move the logic to a `mapper/` class (§7)
  instead of bloating the factory method.
- When a controller method needs more than ~3 related model attributes, or
  builds the same attribute set in multiple handlers, combine them into one
  composite `*View` record (see the `ManageReservationView` example below)
  and bind it under `Attributes.VIEW`. Keep the binding `Form` object
  separate under `Attributes.FORM` — never merge a mutable form and a
  read-only view model into the same record.
- The only mutable, non-record DTO-like class allowed is a session-scoped
  draft object (e.g. `BookingDraft`) that's built incrementally across
  multiple requests. Everything else — forms, views — is a record.

```java
public record ManageReservationView(
        Reservation reservation,
        List<StayingGuest> guests,
        List<RoomAssignment> assignedRooms,
        List<Room> availableRooms,
        Map<Long, Integer> assignedCounts
) {}
```

**Anti-pattern — five loose attributes standing in for one concept:**
```java
model.addAttribute("reservation", reservation);
model.addAttribute("guests", guests);
model.addAttribute("assignedRooms", assignedRooms);
model.addAttribute("availableRooms", availableRooms);
model.addAttribute("assignedCounts", assignedCounts);
```
**Correct:**
```java
model.addAttribute(Attributes.VIEW, buildManageView(id, reservation));
```

---

## 6. Validator Conventions

- `@Component` class with a custom `validate(Form form, BindingResult
  errors)` method — chosen explicitly so validators are invoked where and
  when the controller decides, rather than wired into Spring's automatic
  `Validator`/`supports()` mechanism, which would apply globally and less
  predictably.
- **Do not implement Spring's `Validator` interface.** If you see a textbook
  example suggesting it, that's a different convention than this codebase
  uses — don't "helpfully" convert existing validators to it.
- Field errors: `errors.rejectValue("fieldName", "error.code", "Human
  readable message")`.
- **Skip a downstream check when its own precondition already failed.** Do
  not interpret "return early" as "stop the entire `validate()` method." The
  rule is scoped: never evaluate a derived rule (e.g. date-range logic) if the
  base fields (e.g. individual dates) already have rejections, and never
  evaluate a field's value if the field itself was empty or null. Other
  independent checks in the same `validate()` call still run — only the branch
  whose precondition is broken gets skipped.
- `validate(Form form, BindingResult errors)` is the **single public method**
  on every validator. If a validator handles multiple distinct concerns (cover
  image vs. gallery images, etc.), extract them into **`private`** helper
  methods called from `validate()`. The `validateCoverImage` /
  `validateNewImages` pattern found in `CreateRoomTypeValidator` /
  `EditRoomTypeValidator` is legacy — those helpers should be `private`, not
  `public`. Migrate on touch.
- Validators that need uniqueness or cross-entity checks (e.g. "email
  already in use") inject the relevant repository directly — that's the
  one place repositories are injected outside `service/`.

---

## 7. Mapper / Static Utility Conventions

- Static utility class, **private constructor** to prevent instantiation.
- `@NoArgsConstructor` is not used here — write the private constructor
  explicitly.
- `mapper/` hosts two kinds of classes:
  - **Mapping utilities** — transform entities/objects into view models.
    Methods follow `public static *View toXxx(Entity entity, ...)` naming.
    **Pure functions only** — no dependency injection, no repository calls, no
    mutable state, no side effects. If a mapping needs a repository lookup
    (e.g. resolving a related entity by ID), that lookup happens in the
    service before calling the mapper, and the resolved object is passed in.
    Any field that requires computation (string formatting, derived counts,
    conditional display logic) lives in the mapper, not in the controller and
    not in the template. Templates render data; they don't compute it.
  - **Non-mapping static utilities** — e.g. session accessors, formatting
    helpers. Must still have a private constructor and `final` class, but are
    exempt from the pure-function and `toXxx` naming constraints.

```java
public class ReservationMapper {

    private ReservationMapper() {}

    public static ManageReservationView toManageView(
            Reservation reservation,
            List<StayingGuest> guests,
            List<RoomAssignment> assignedRooms,
            List<Room> availableRooms) {
        var assignedCounts = assignedRooms.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getRoom().getRoomType().getId(),
                        Collectors.summingInt(a -> 1)));
        return new ManageReservationView(
                reservation, guests, assignedRooms, availableRooms, assignedCounts);
    }
}
```

**Anti-pattern — computing display values in templates:**
```html
<span th:text="${guest.firstName + ' ' + guest.lastName}">
```
**Correct:** compute `displayName` in the mapper as a field on the `*View`
record.

---

## 8. Template Layout

```
templates/
├── fragments/
│   ├── admin-sidebar.html
│   ├── manager-sidebar.html
│   ├── receptionist-sidebar.html
│   ├── form-error.html
│   ├── credential-content.html
│   └── components/
│       └── toast.html
├── {role}/
│   ├── {feature}/
│   │   └── *.html
│   └── *.html
├── guest/
│   ├── layout.html
│   ├── booking/
│   │   └── *.html
│   └── *.html
└── error/
    ├── 401.html
    ├── 403.html
    ├── 404.html
    ├── 500.html
    └── 400.html
```

- A template never contains business logic or computed expressions beyond
  trivial display formatting (`#{...}` message lookups, simple
  conditionals on already-computed booleans). If a template needs a ternary
  doing real logic, that logic belongs in the mapper or view model instead.
- Fragment names are stable contracts — changing a fragment's expected
  parameters is a breaking change across every feature that includes it;
  grep for usages before modifying.

**Guest layout note:** Guest-facing pages use `guest/layout.html`
(`th:fragment="header(activePage)"`), a top navigation bar, not a sidebar.
Guest controllers set an `activePage` model attribute (e.g. `"home"`,
`"rooms"`, `"policies"`, `"contact"`) that the layout uses to highlight the
current nav link via `th:classappend`. Unlike staff controllers, guest
controllers do not set `Attributes.ACTIVE_SIDEBAR` or define an
`addCommonAttributes` method — each handler sets `activePage` or the
template receives it implicitly through the view rendering chain.

---

## 9. Shared Cross-Cutting Conventions

- **`Attributes`** — constants class holding all model-attribute keys
  (`FORM`, `VIEW`, `SUCCESS`, `ERROR`, `ACTIVE_SIDEBAR`, etc.). Never use a
  raw string literal for a model attribute key in a controller — always
  reference `Attributes.X`. This was inconsistent in earlier code (some
  handlers used `"success"` literally); going forward, raw literals here
  are a lint-worthy mistake, not a style choice.
- **`HotelConstants.HOTEL_ID = 1L`** — this is a deliberate single-tenant
  simplification for the current scope of the app, not a placeholder. Do
  not "fix" it into a multi-tenant resolution mechanism without an explicit
  request to do so.
- **Exception → HTTP/UX mapping** is centralized in `GlobalExceptionHandler`:
  - `ResourceNotFoundException` → renders the 404 error page.
  - `IllegalStateException` / `IllegalArgumentException` → redirected back
    with `Attributes.ERROR` flash message, when thrown from a context where
    the controller didn't already catch and handle them locally.
  - Controllers should prefer letting these propagate to the global handler
    over local try/catch, **except** when:
    1. The controller also needs to set a success-path flash message in the
       same method (see §2's `assignRoom` example) — local try/catch is
       correct because the global handler can't express "this succeeded, say
       so", **or**
    2. The error path requires re-rendering the current view with model
       attributes populated (e.g. a form page with inline error) — local
       try/catch with re-render is correct because the global handler's
       redirect would lose the model context.
- **Sidebar key mapping** — each staff feature's `@ModelAttribute` sets
  `Attributes.ACTIVE_SIDEBAR` to one of a fixed set of constants matching
  the sidebar fragment's expected keys (e.g. `"RESERVATIONS"`, `"ROOMS"`,
  `"GUESTS"`). Don't invent new keys without adding the corresponding
  sidebar entry in the fragment. Guest controllers do not use this — see §8.
- **Flash message pattern**: exactly one of `Attributes.SUCCESS` /
  `Attributes.ERROR` per redirect, never both, message text is
  user-facing (no stack traces, no internal codes).

---

## 10. Testing Conventions

- Service tests: `@ExtendWith(MockitoExtension.class)`, mock repositories,
  no Spring context unless the test specifically needs transactional
  behavior — in that case use a slice test, not a full `@SpringBootTest`.
- Controller tests: `@WebMvcTest(SomeController.class)` with mocked
  services, asserting view name, model attributes, and redirect/flash
  behavior — not hitting the database.
- DTO unit tests: plain JUnit 5, no mocking, test validation annotation
  behavior directly (e.g. `ValidatorFactory validator =
  Validation.buildDefaultValidatorFactory()`).
- Test class dependencies: `@RequiredArgsConstructor` is fine for test
  classes that need constructor injection (MockitoExtension handles it).
  Manual construction with `new` and mocks is also fine.
- Naming: `should_doX_when_conditionY` or `methodName_condition_expected` —
  pick one and stay consistent within a test class; don't mix styles in the
  same file.
- A new ownership-check branch (§3) must have a test asserting the
  mismatched-parent case throws — this is the exact bug class this doc
  exists to prevent, so it's the one piece of behavior that's non-negotiable
  to cover.

---

## 11. Logging Conventions

- SLF4J via Lombok's `@Slf4j` on the class — no `System.out.println`, ever.
- `WARN` for expected business-rule rejections (caught
  `IllegalStateException`/`IllegalArgumentException` cases) — these are
  normal operation, not failures of the system.
- `ERROR` only for genuinely unexpected exceptions (anything not part of the
  declared business-rule vocabulary above).
- Never log full entity objects (PII risk — guest names, dates of birth).
  Log IDs and the specific fields relevant to the message.

---

## 12. Dependency Injection

- `@RequiredArgsConstructor` **everywhere** — controllers, services,
  validators, any Spring-managed bean. This is the project standard.
  Manual constructors are tech debt; migrate them on touch.
- No `@Autowired` on fields, no field injection, no setter injection.
  If you see field injection, it's a bug — fix it.
- The only classes that don't use `@RequiredArgsConstructor` are:
  - Entities — use `@Getter @Setter` only
  - Mappers — static utility with explicit private constructor
  - Configuration classes / `@Bean` factory methods — those use plain Java
    or method parameters as appropriate

---

## 13. Security & Authorization

- **Roles** are defined in the `Role` enum: `ADMIN`, `LEADER`, `RECEPTIONIST`,
  `MANAGER`. No `Permission` enum or granular permission system exists —
  authorization is purely role-based.
- **URL-based security** in `SecurityConfig.java` using `HttpSecurity`
  request matchers. No `@PreAuthorize`, `@Secured`, or `@RolesAllowed`
  method-security annotations are used anywhere. Do not introduce
  method-security annotations — all authorization lives in the filter chain.
  - `/admin/**` → `hasRole("ADMIN")`
  - `/manager/**` → `hasRole("MANAGER")`
  - `/receptionist/**` → `hasAnyRole("RECEPTIONIST", "ADMIN")`
  - Public pages (`/`, `/rooms`, `/book`, `/my-booking/**`, `/login`,
    `/auth/password/**`, `/css/**`, etc.) → `permitAll()`
  - All other requests → `authenticated()`
- **Known gaps:** `/leader/**` has no `.requestMatchers` rule in
  `SecurityConfig`. A `LEADER` user is authenticated but has no dedicated
  protected namespace. `SecurityConfig` also has a duplicate `.formLogin()`
  call (line 77) that is overridden by the second call at line 81 — the
  second call is the effective one. Fix both on touch.
- **Current user** is accessed via `@AuthenticationPrincipal AccountPrincipal
  principal` on controller method parameters — never via
  `SecurityContextHolder` directly.
- **Password hashing:** Argon2 via
  `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()` as the sole
  `PasswordEncoder` bean. No bcrypt, PBKDF2, or scrypt.
- **Bouncy Castle** is declared in `pom.xml` but never imported or invoked in
  any `.java` file. It may be pulled in transitively by Spring Security's
  Argon2 implementation. Do not add direct imports of Bouncy Castle APIs —
  all cryptographic operations go through Spring Security's `PasswordEncoder`.
- **CSRF protection** is provided by `spring-security` and auto-injected into
  every `<form th:action="...">` via `thymeleaf-extras-springsecurity6` (which
  is on the classpath through Spring Boot's security starter). **Do not disable
  CSRF in `SecurityConfig`** — the auto-configuration handles token injection
  and verification transparently. If a POST endpoint returns 403, verify that
  the form uses `th:action` (not plain `action`) and that
  `thymeleaf-extras-springsecurity6` is on the classpath, rather than
  disabling CSRF.
- **No `RoleHierarchy`** is configured. `hasRole()` / `hasAnyRole()` checks
  are explicit. Do not add a role hierarchy without discussion.
- **Post-login redirect** is handled by
  `RoleBasedAuthenticationSuccessHandler`: reads the first granted authority
  and redirects to `/{role.lowercase}`. Template-driven sidebar inclusion
  (not role-dynamic) — each template explicitly references its sidebar
  fragment via `th:replace`.
- **Sensitive data:** Guest PII (name, email, phone, date of birth,
  nationality) is stored in plaintext in `Reservation` and `StayingGuest`
  entities. No field-level encryption, masking, or anonymization exists. This
  is an accepted constraint for the current single-hotel, server-rendered
  scope — do not add encryption or data-protection infrastructure without an
  explicit request.

---

## 14. File Upload & Image Processing

- **Accepted upload formats:** `image/jpeg`, `image/png`, `image/webp` only.
  Validated at the validator layer (not at the `spring.servlet.multipart`
  level).
- **Max file size:** 5 MB per file (validated in `CreateRoomTypeValidator`
  / `EditRoomTypeValidator`). Gallery limit: 5 images total per room type
  (existing + new).
- **`spring.servlet.multipart.*`** is not configured in any properties file.
  Spring Boot defaults apply (`max-file-size=1MB`, `max-request-size=10MB`).
  If a real upload exceeds these defaults, configure them in the target
  profile; do not raise them globally.
- **All uploaded images are converted to WebP.** No original format is
  preserved. Conversion parameters:
  - Room type **cover**: 1080px width, quality = 0.75
  - Room type **gallery**: 720px width, quality = 0.75
  - **Hotel** images: 720px width, quality = 0.75
  Target width constants belong in the service class that owns the
  conversion — not in the utility class.
- **Storage:** `byte[]` in a PostgreSQL `BYTEA` column with
  `@Basic(fetch = FetchType.LAZY)`. This is a **deliberate design choice**
  for a single-instance, server-rendered application with no CDN — not tech
  debt. Do not refactor to filesystem storage or S3 without an explicit
  request.
- **Serving:** Dedicated `@Controller` endpoints return `ResponseEntity<byte[]>`
  with `Content-Type: image/webp`. Each image entity type has its own
  controller (`RoomTypePictureController`, `HotelPictureController`).
  Template references: `<img th:src="@{'/room-type-picture/' + ${id}}">`.
- **Conversion utilities:** Two parallel implementations exist —
  `ImageCompressionService` (Spring `@Service`) and `ImageUtils` (static
  utility). Both do the same thing: resize + `ImageIO.getImageWritersByFormatName("webp")`
  + write with compression quality. This is tech debt — consolidate into a
  single utility class on touch. The surviving class should follow the
  **static utility** pattern (§7), not a Spring-managed service, because
  image conversion has no dependencies.
- **Hotel images** currently have no MIME type or size validation at the
  service layer — only a null/empty check in `HotelService.update()`. This is
  a gap; add validation comparable to `CreateRoomTypeValidator` on touch.

---

## 15. Database & Schema

- **Schema management:** Hibernate `ddl-auto=update` only. No Flyway,
  Liquibase, or any migration tool is used. The 14 `@Entity` classes define
  the entire schema. Do not introduce a migration tool without an explicit
  request.
- **Binary storage:** `@Column(columnDefinition = "BYTEA")` for image data —
  not `@Lob`. `@Lob` is not used anywhere in this codebase. All binary
  columns are `byte[]` typed.
- **Single PostgreSQL datasource** across all profiles (dev/prod point to
  the same database). No H2 in-memory database for tests — tests interact
  with the same PostgreSQL instance or mock the repository layer.
- **`spring.jpa.show-sql=true`** is set globally in the base
  `application.properties`. If it becomes noisy in production, move it to
  `application-dev.properties` on touch — do not delete it entirely, as it's
  useful for debugging during development.
- **Connection pool:** HikariCP (Spring Boot default) — no custom
  `spring.datasource.type` or pool tuning is configured.
- **Entity count:** 14 entities — see the entity inventory in the original
  schema design doc or grep for `@Entity` annotations. All use
  `GenerationType.IDENTITY` for primary keys.

---

## 16. Configuration & Profiles

- **Format:** `.yml` files only. No `.properties` files in new code — existing
  `.properties` files were migrated to YAML.
- **Active profile:** `dev` is active by default
  (`spring.profiles.active=dev` in `application.yml`).
  Switching to `prod` requires an explicit CLI flag
  (`--spring.profiles.active=prod`) or environment variable.
- **Profile-specific overrides:** Only one property differs between profiles:
  - `dev`: `app.mail.console: true`
  - `prod`: `app.mail.console: false`
- **No `application-test.yml`** exists. Tests share the same
  datasource configuration as the dev profile or mock persistence entirely
  (see §10).
- **Property injection:** `@ConfigurationProperties` preferred for groups of
  2+ related properties. `PayPalProperties` (`paypal.*`) uses this pattern.
  `@Value` remains for single one-off values:
  - `app.mail.console` in `EmailServiceImpl`
  - `app.security.remember-me-key` in `SecurityConfig`
- **Example file:** `application-example.yml` is a blank template
  for deployment. Keep it in sync with any new properties added to the base
  file.

---

## 17. Alpine.js Conventions

- **Version:** Alpine.js 3.14.9 (via cdnjs) or 3.x latest (via jsdelivr).
  No npm / webpack bundle. Both CDN sources are acceptable, but prefer one
  per template — do not load both.
- **Separation of concerns:** Thymeleaf renders the DOM and controls
  conditional rendering (`th:if`, `th:unless`). Alpine handles client-side
  reactivity, animations, and transient UI state (modal open/close, toast
  visibility, checkbox confirmation). Do not use Alpine for data that must
  survive a page reload — that belongs in Thymeleaf-rendered model
  attributes or flash attributes.
- **Data bridge:** When Alpine needs server-rendered data, use
  `th:inline="javascript"` to write the data into global JS variables, then
  reference those variables in `x-data`:
  ```html
  <script th:inline="javascript">
      var ROOMS_DATA = /*[[${availability}]]*/ [];
      var MAX_ROOMS = /*[[${maxRooms}]]*/ 10;
  </script>
  <div x-data="{ rooms: ROOMS_DATA, maxRooms: MAX_ROOMS, ... }">...</div>
  ```
  Never use `th:attr` to generate Alpine `x-bind:` attributes dynamically —
  Alpine bindings are always written as static HTML attributes.
- **Shared components:**
  - **Toast:** `fragments/components/toast.html` — the only shared Alpine
    component. Included via `th:replace` on every page that needs flash
    message display. Uses `x-data`, `x-init`, `x-show`, `x-transition` for
    auto-dismiss animation.
  - **Image uploaders:** `Alpine.data("imageUploader", ...)` registered in
    `static/js/components/image-uploader.js`. Page-specific variants
    (`coverUploader`, `imageStack`) are registered inline via `<script>` in
    the template — prefer the external `.js` file pattern for any new
    reusable component.
- **Modals** follow a single standard pattern:
  ```html
  <div x-data="{ modalOpen: false }">
      <button @click="modalOpen = true">Open</button>
      <div x-show="modalOpen"
           @click.self="modalOpen = false"
           x-transition:enter="..." x-transition:leave="...">
          <div @click.stop>
              <!-- modal content -->
              <button @click="modalOpen = false">Close</button>
          </div>
      </div>
  </div>
  ```
  Backdrop click closes (`@click.self`), modal content click does not bubble
  (`@click.stop`). `x-cloak` must be defined in a `<style>` block on any page
  using `x-show` to prevent FOUC.
- **CSRF bridge:** Any page that makes `fetch()` calls from Alpine (currently
  only `manager/room/list.html`) must include:
  ```html
  <meta name="_csrf" th:content="${_csrf.token}">
  <meta name="_csrf_header" th:content="${_csrf.headerName}">
  <script th:inline="javascript">window.csrfToken = /*[[${_csrf.token}]]*/ '';</script>
  ```
  Pass the token as a form-encoded parameter in the POST body:
  `params.append('_csrf', window.csrfToken)`.
- **Prohibited patterns** — must not be introduced without discussion:
  `$store`, `$dispatch`, `x-html`, client-side routing, or any fetch-based
  navigation that bypasses the MVC controller flow.

---

## 18. Pagination

- **No pagination exists in the current codebase.** Every list view
  (rooms, room types, reservations, accounts) uses `findAll()` /
  `findBy...()` repository methods that load the entire result set as
  `List<T>`. This works for the current data volume of a single hotel.
- **When pagination is introduced** (for any view where the unfiltered
  dataset exceeds ~100 rows or significantly degrades page load time), it
  must use Spring Data's `Pageable` / `Page` abstractions at the repository
  layer — never client-side pagination via JavaScript, and never manual
  `OFFSET`/`LIMIT` in custom queries.
- **Controller signature** for paginated endpoints:
  ```java
  @GetMapping
  String list(@PageableDefault(size = 20, sort = "checkInAt") Pageable pageable, Model model) {
      model.addAttribute(Attributes.VIEW, reservationService.findAllPaged(pageable));
      ...
  }
  ```
- **Template rendering** uses server-rendered page links in Thymeleaf, not
  JavaScript. Provide page-number links and a total-count label. Do not rely
  on infinite-scroll or AJAX-powered pagination — the app is server-rendered.
- **`findAll()` is tech debt** for any view that has pagination-capable
  infrastructure but doesn't use it. If you touch a repository method that
  currently returns `List<T>` and the caller displays data that could exceed
  100 rows, introduce a `Page<T>` alternative — do not add more non-paginated
  queries for high-volume data.
