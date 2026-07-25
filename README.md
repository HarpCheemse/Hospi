# Hospi

Hotel management web application built with Spring Boot 3.5, Thymeleaf, and PostgreSQL.

## Quick start
- Build: `mvn compile` (no Maven wrapper; system Maven 3.9+ required)
- Test all: `mvn test -q`
- Single test: `mvn test -Dtest=ClassName#methodName -q -Dspring.main.banner-mode=off`
- Single class: `mvn test -Dtest=ClassName -q -Dspring.main.banner-mode=off`
- Package: `mvn package -DskipTests`
- Dev server: run `HospiApplication.java` in your IDE (Spring Boot 3.5)
- Profile `dev` is active by default (`spring.profiles.active=dev`)
- CSS: `npm run css:build` (one-time) or `npm run css:watch` (watch mode)

## Prerequisites
- Java 17
- Apache Maven 3.9+
- Node.js (for Tailwind CSS build; see `package.json`)
- PostgreSQL with a database `hospi_hotel` created

## Database setup
1. Create the database: `createdb hospi_hotel`
2. Tables are auto-created by Hibernate (`ddl-auto=update`)
3. Seed data for development: run `docs/seed-test-data.sql`
4. DDL reference: `docs/tables.sql`

## Config files are gitignored
`application.yml`, `application-dev.yml`, `application-prod.yml` are in `.gitignore` — they exist locally but are NOT committed. Use `application-example.yml` as a template for new properties. Do not rely on these files being in git.

## Testing conventions
- **No `@SpringBootTest`** — all tests use mock-based approaches
- **Service tests:** `@ExtendWith(MockitoExtension.class)`, mock repositories only, no Spring context
- **Controller slice tests:** `@WebMvcTest(SomeController.class)` + `@AutoConfigureMockMvc(addFilters = false)` with `@MockitoBean` services
- **DTO tests:** plain JUnit 5, test Jakarta Validation constraint annotations
- **Test naming:** `should_doX_when_conditionY` 
- See `docs/project-structure.md` §10 for the full authoritative testing strategy

### Controller test requirements
Every controller handler must have tests covering:
- **GET handlers:** verify page renders (HTTP 200 + correct view name + rendered HTML contains expected text via `content().string(containsString("..."))`)
- **POST success:** verify redirect URL + flash attribute
- **POST validation failure:** verify re-render (view returned without redirect) + correct binding/message
- **POST business-rule failure:** verify redirect + `ERROR` flash
- **Redirect guards:** null/missing session state → appropriate redirect
- See `BookingFlowControllerTest.java` for the canonical example

## Code conventions (`docs/project-structure.md` is authoritative)
- Feature modules under `features/{feature}/` with substructure: `controller/`, `service/`, `entity/`, `dto/`, `repository/`, `mapper/`, `validation/`, `enums/`
- Beans: `@RequiredArgsConstructor` + `private final` — never `@Autowired`, no field injection
- Entities: `@Getter @Setter` (never `@Data`), explicit `@Column(name = "snake_case")`, `@Enumerated(EnumType.STRING)`
- Controllers: `@Controller` (not `@RestController`), class-level `@RequestMapping`, redirect-after-POST, exactly one flash attribute
- Services: `@Transactional` on write methods, `findById` throws `ResourceNotFoundException` on miss, `IllegalStateException` for business rule violations
- Validators: `@Component` with `validate(Form, BindingResult)` — do NOT implement Spring's `Validator` interface
- Mappers: static utility class with private constructor, pure functions, no DI

## Architecture notes
- Server-rendered only — no REST API, no `@RestController`, no AJAX navigation
- Single tenant: `HotelConstants.HOTEL_ID = 1L` — deliberate, not a placeholder
- Images: `byte[]` in `BYTEA` columns, served via dedicated `@Controller` endpoints, WebP conversion
- Schema: Hibernate `ddl-auto=update` — no Flyway/Liquibase
- Security: URL-based in `SecurityConfig.java` — no `@PreAuthorize` / `@Secured` / method-security annotations
- Passwords: Argon2 via `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`

## CSS / UI (see `docs/STYLING.md` for full spec)
- Tailwind v4 with `@theme` token blocks in `src/main/frontend/theme/*.css`
- Component classes at `@layer components` in `src/main/frontend/components/*.css` (`.btn`, `.card`, `.pill`)
- Never edit `src/main/resources/static/css/app.css` — it's generated
- Never use raw hex colors in templates — use semantic tokens (`bg-primary`, `text-muted-foreground`, etc.)
- Always use `.btn .btn-{variant}` for buttons, never raw utility classes

## Directory orientation
- `leader/` feature module is an empty directory (exists for routing, no code yet)
- `receptionist/` has skeleton templates only — no controllers, services, or entities
- `auth/dto/` and `auth/validator/` are empty (planning structure, not yet populated)
- No CI / GitHub Actions configured

## Documentation
| File | Contents |
|---|---|
| `docs/project-structure.md` | Full code conventions — structure, controllers, services, entities, DTOs, templates, testing, logging, DI, security, file upload, database, config, Alpine.js |
| `docs/SDS.md` | Architecture overview, routes, entity relationships, design constraints |
| `docs/STYLING.md` | Design system — Tailwind v4 tokens, component classes, layout patterns, DO/DON'T |
| `docs/tables.sql` | Full database DDL |
| `docs/seed-test-data.sql` | Development seed data |
