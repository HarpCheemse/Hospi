# Hospi Design System

> Single source of truth for UI development. All visual decisions, design tokens, component patterns, and usage rules live here.

---

## 1. Design System Overview

### 1.1 Stack

| Layer     | Technology                                                  |
|-----------|-------------------------------------------------------------|
| Framework | Tailwind CSS v4.3.0                                         |
| CLI       | `@tailwindcss/cli`                                          |
| Input     | `src/main/frontend/app.css`                                 |
| Output    | `src/main/resources/static/css/app.css` (minified, generated) |
| Runtime   | Node.js (build-time only)                                   |

### 1.2 Build Pipeline

```
src/main/frontend/app.css
  │  @import "tailwindcss"
  │  @import "./theme/colors.css"
  │  @import "./theme/typography.css"
  │  @import "./theme/spacing.css"
  │  @import "./theme/shadows.css"
  │  @import "./theme/motion.css"
  │  @import "./components/layout.css"
  │  @import "./components/button.css"
  ▼
tailwindcss CLI (--minify)
  ▼
src/main/resources/static/css/app.css   ← NEVER edit manually
```

Each theme file is a standalone `@theme {}` block. Tailwind v4 converts these custom properties into utility classes automatically (e.g. `--color-primary` → `bg-primary`, `text-primary`, `border-primary`).

**Commands:**

| Command             | Action                                |
|---------------------|---------------------------------------|
| `npm run css:build` | One-time production build (minified)  |
| `npm run css:watch` | Watch mode — rebuilds on file changes |

### 1.3 Content Scanning

Tailwind scans these paths to detect used class names:

| Path                                     | Scans                 |
|------------------------------------------|-----------------------|
| `src/main/resources/templates/**/*.html` | Thymeleaf views       |
| `src/main/webapp/**/*.jsp`               | JSP partials (1 file) |
| `src/main/webapp/**/*.html`              | Static HTML           |
| `src/main/**/*.{js,ts}`                  | Frontend scripts      |

### 1.4 Generated Output Rules

- `src/main/resources/static/css/app.css` is **generated** — never edit manually.
- No handwritten CSS files beyond the `theme/` and `components/` source files.
- No inline `style` attributes in templates.
- No external CSS frameworks alongside Tailwind.

---

## 2. Design Tokens

> All tokens are defined in `src/main/frontend/theme/*.css` via `@theme {}` blocks.

### 2.1 Color Palette

**Source:** `src/main/frontend/theme/colors.css`

#### Raw Swatches

| Swatch     | Hex       | Role                              |
|------------|-----------|-----------------------------------|
| `ink`      | `#17211F` | Darkest — foreground, text        |
| `forest`   | `#123C35` | Dark primary (button gradients)   |
| `emerald`  | `#1F6F5B` | Primary brand green               |
| `gold`     | `#C7A45D` | Accent — prices, premium          |
| `ivory`    | `#FBF8F1` | Page background                   |
| `porcelain`| `#FFFFFF` | Card/surface white                |
| `stone`    | `#6B6258` | Reserved — not directly used      |

#### Semantic Colors

These are the only tokens you should reference in templates. Every `--color-{x}` produces `bg-{x}`, `text-{x}`, `border-{x}`, etc.

| Token                | Value     | Role                | Produces                        |
|----------------------|-----------|---------------------|---------------------------------|
| `primary`            | `#1F6F5B` | Brand primary       | `bg-primary text-primary ...`   |
| `primary-foreground` | `#FBF8F1` | Text on primary     | `text-primary-foreground`       |
| `accent`             | `#C7A45D` | Premium emphasis    | `bg-accent text-accent ...`     |
| `accent-foreground`  | `#17211F` | Text on accent      | `text-accent-foreground`        |
| `background`         | `#FBF8F1` | Page backdrop       | `bg-background`                 |
| `foreground`         | `#17211F` | Default text        | `text-foreground`               |
| `surface`            | `#FFFFFF` | Card/panel base     | `bg-surface`                    |
| `surface-foreground` | `#17211F` | Text on surface     | `text-surface-foreground`       |
| `muted`              | `#EEE7DA` | Muted background    | `bg-muted` — warm panel bg      |
| `muted-foreground`   | `#aeaeae` | Muted text          | `text-muted-foreground`         |
| `border`             | `#DED5C7` | Dividers/outlines   | `border-border`                 |

**Important semantic change:** `muted` is now a background color (`#EEE7DA`, warm beige). Use `muted-foreground` (`#aeaeae`) for secondary/muted text. The old `text-muted` (`#6B6258`) has been replaced — use `text-muted-foreground` or `text-foreground` with opacity instead.

#### Foreground Companion Convention

Every semantic color that may serve as a background has a matching `*-foreground` token for text contrast:

| Background token     | Foreground token         | Typical use                    |
|----------------------|--------------------------|--------------------------------|
| `bg-primary`         | `text-primary-foreground`| Primary buttons                |
| `bg-accent`          | `text-accent-foreground` | Accent labels                  |
| `bg-surface`         | `text-surface-foreground`| Card body text                 |
| `bg-background`      | `text-foreground`        | Page body text                 |
| `bg-muted`           | `text-muted-foreground`  | Muted panel content            |

#### State Colors

Reserved for **status communication only** — never use for decoration.

| Token                | Value     | Usage                                              |
|----------------------|-----------|----------------------------------------------------|
| `success`            | `#2F7D4F` | Completed, available, confirmed, clean             |
| `success-foreground` | `#FFFFFF` | Text on `bg-success`                               |
| `warning`            | `#B7791F` | Pending, needs review, limited, dirty              |
| `warning-foreground` | `#FFFFFF` | Text on `bg-warning`                               |
| `danger`             | `#B84A3A` | Destructive actions, failed, conflict, maintenance |
| `danger-foreground`  | `#FFFFFF` | Text on `bg-danger`                                |

**Conventions for state indicators:**

```html
<!-- Background tint (Tailwind v4 opacity modifier) -->
<span class="bg-success/10 text-success ...">Active</span>
<span class="bg-danger/10 text-danger ...">Disabled</span>
<span class="bg-warning/10 text-warning ...">Pending</span>

<!-- Solid state background -->
<button class="bg-danger text-danger-foreground ...">Delete</button>
```

**Note on `--color-info`:** There is no `info` color token in the current palette. Use `--color-primary` or `--color-muted-foreground` for neutral informational states. If needed, add via `@theme {}` in `colors.css`.

#### DO NOT

- ❌ Write `#1F6F5B` or any raw hex in template class attributes
- ❌ Use `text-green-600` or `bg-green-100` — use semantic tokens (`text-success`, `bg-success/10`) instead
- ❌ Use state colors (`success`, `danger`, `warning`) for decorative elements

### 2.2 Typography

**Source:** `src/main/frontend/theme/typography.css`

#### Font Families

| Utility        | Stack                                                       | Usage                                                                      |
|----------------|-------------------------------------------------------------|----------------------------------------------------------------------------|
| `font-sans`    | `"DM Sans", ui-sans-serif, system-ui, sans-serif`           | **Default.** Body text, UI labels, data tables, buttons                    |
| `font-display` | `"Cormorant Garamond", Georgia, serif`                      | **Public pages only.** Headings, hero text (unused — available for future) |
| `font-mono`    | `"JetBrains Mono", ui-monospace, SFMono-Regular, monospace` | Code snippets, numeric data where monospaced alignment matters             |

Tailwind v4 sets `font-sans` as the default. `font-display` is defined but **not used** in current templates.

#### Font Size Scale

Tailwind v4 derives line-heights automatically from the size value. Do not add `leading-*` unless overriding a specific context.

| Token       | Size               | Common usage                                            |
|-------------|--------------------|---------------------------------------------------------|
| `text-2xs`  | `0.6875rem` (11px) | Overline labels, badge text                             |
| `text-xs`   | `0.75rem` (12px)   | Metadata, timestamps, help text                         |
| `text-sm`   | `0.875rem` (14px)  | **Default body.** Descriptions, table cells, input text |
| `text-base` | `1rem` (16px)      | Longer-form content                                     |
| `text-lg`   | `1.125rem` (18px)  | Section headings, card titles                           |
| `text-xl`   | `1.25rem` (20px)   | Sub-page headings                                       |
| `text-2xl`  | `1.5rem` (24px)    | Page titles (`h1`)                                      |
| `text-3xl`  | `1.875rem` (30px)  | Hero titles, large stats                                |
| `text-4xl`  | `2.5rem` (40px)    | Public landing page only                                |
| `text-5xl`  | `3.25rem` (52px)   | Public landing page only                                |
| `text-6xl`  | `4rem` (64px)      | Public landing page only                                |

**Rule:** Staff-facing pages use `text-sm` as body size (14px). Never use `text-base` for operational UI.

#### Font Weights

Use Tailwind's default weight utilities. No custom weights are defined.

| Token                 | When to use                                                      |
|-----------------------|------------------------------------------------------------------|
| `font-bold` (700)     | Page titles (`h1`), card headers, metric values, price displays  |
| `font-semibold` (600) | Section headers (`h2`), button labels, input labels, stat values |
| `font-medium` (500)   | Table row labels, list item titles, navigation items             |
| `font-normal` (400)   | Body text, descriptions, paragraphs                              |

#### Letter Spacing

| Token               | Value    | Usage                                           |
|---------------------|----------|-------------------------------------------------|
| `tracking-brand`    | `0.08em` | Overline labels, uppercase section headings      |
| `tracking-brand-wide` | `0.16em` | Badges, suite labels, prominent uppercase text |

### 2.3 Spacing & Layout

**Source:** `src/main/frontend/theme/spacing.css`

#### Border Radius

| Token          | Value    | Usage                                                         |
|----------------|----------|---------------------------------------------------------------|
| `rounded-sm`   | 0.375rem | Nested elements, inline indicators                            |
| `rounded`      | 0.5rem   | Minimal rounding                                              |
| `rounded-md`   | 0.625rem | Minor elements                                                |
| `rounded-lg`   | 0.75rem  | Button corners (used in `.btn` component)                     |
| `rounded-xl`   | 1rem     | **Standard.** Inputs, stat cards, compact panels              |
| `rounded-2xl`  | 1.25rem  | **Elevated.** Section cards, panels, modals, large containers |

**Rule of thumb:** `rounded-xl` for inputs and compact cards; `rounded-2xl` for top-level containers.

#### Layout Tokens

| Token                     | Value    | CSS/Utility              | Usage                                          |
|---------------------------|----------|--------------------------|-------------------------------------------------|
| `layout-sidebar-width`    | `16rem`  | (reference value)        | Sidebar width in staff layout (`w-64`)          |
| `layout-section-gap`      | `3rem`   | (reference value)        | Vertical spacing between sections (`gap-12`)    |
| `layout-prose-width`      | `42rem`  | `max-w-prose`            | Constrained text content                        |
| `layout-content-width`    | `72rem`  | `max-w-[72rem]`          | Max content width for wide layouts              |

#### Modal & Drawer Sizes

| Token           | Value    | Common use                       |
|-----------------|----------|----------------------------------|
| `modal-width-sm`| `24rem`  | Small confirmations              |
| `modal-width`   | `36rem`  | Standard modals                  |
| `modal-width-lg`| `48rem`  | Large content modals             |
| `drawer-width`  | `24rem`  | Slide-in drawer panels           |

#### Breakpoints

| Token     | Value   | Usage                         |
|-----------|---------|-------------------------------|
| `xs`      | `480px` | Small mobile devices          |
| `sm`      | `640px` | Large mobile / small tablet   |
| `md`      | `768px` | Tablet                        |
| `lg`      | `1024px`| Desktop                       |
| `xl`      | `1280px`| Wide desktop                  |
| `2xl`     | `1536px`| Extra-wide desktop            |

Used as `sm:flex`, `lg:grid-cols-3`, etc.

#### Z-Index Layering

| Token        | Value | Usage                            |
|--------------|-------|----------------------------------|
| `z-dropdown` | 100   | Dropdown menus, popovers         |
| `z-sticky`   | 200   | Sticky headers, sticky elements  |
| `z-drawer`   | 300   | Slide-in drawer panels           |
| `z-modal`    | 400   | Modal dialogs                    |
| `z-toast`    | 500   | Toast notifications              |
| `z-tooltip`  | 600   | Tooltips (highest)               |

Use instead of raw `z-10`, `z-50`, etc.

#### Standard Spacing Scale

Tailwind v4's default spacing scale (`0.25rem` increments). Common values in use:

| Spacing                 | Rem          | px      | Context                          |
|-------------------------|--------------|---------|----------------------------------|
| `gap-1` / `space-y-1`   | 0.25rem      | 4px     | Navigation item spacing          |
| `gap-3`                 | 0.75rem      | 12px    | Button groups, inline elements   |
| `gap-4` / `p-4`         | 1rem         | 16px    | Compact cards, panel padding     |
| `gap-5` / `p-5`         | 1.25rem      | 20px    | Form grid cells, default padding |
| `gap-6` / `p-6`         | 1.5rem       | 24px    | Page content padding, card gaps  |
| `mb-6` / `mt-6`         | 1.5rem       | 24px    | Section vertical gaps            |
| `mb-8`                  | 2rem         | 32px    | Logo-to-nav spacing in sidebar   |

**Rule:** Use semantic spacing consistently. Avoid arbitrary values.

### 2.4 Box Shadows (Elevation)

**Source:** `src/main/frontend/theme/shadows.css`

| Token          | Value                             | Elevation   | Usage                              |
|----------------|-----------------------------------|-------------|------------------------------------|
| `shadow-card`  | `0 12px 32px rgba(23,33,31,0.08)` | **Default** | Cards, panels, tables              |
| `shadow-soft`  | `0 18px 48px rgba(23,33,31,0.10)` | Elevated    | Toasts, dropdowns, modal backdrops |
| `shadow-float` | `0 24px 64px rgba(23,33,31,0.14)` | Floating    | Modals, floating menus (unused)    |
| `shadow-inset` | `inset 0 2px 6px rgba(23,33,31,0.08)` | Inset   | Pressed states, inset containers   |
| `shadow-none`  | `none`                            | —           | Explicitly remove shadows          |

**Rule:** Every card, panel, and section container gets `shadow-card`. Do not use `shadow-sm`, `shadow-md`, or Tailwind defaults — use the project tokens.

### 2.5 Motion

**Source:** `src/main/frontend/theme/motion.css`

#### Duration

| Token            | Value | Usage                                 |
|------------------|-------|---------------------------------------|
| `duration-fast`  | 120ms | Button press, micro-interactions      |
| `duration-base`  | 180ms | Standard hover/transition             |
| `duration-slow`  | 360ms | Modal enter, page transitions         |

#### Easing

| Token           | Value                            | Usage                          |
|-----------------|----------------------------------|--------------------------------|
| `ease-standard` | `cubic-bezier(0.22, 1, 0.36, 1)` | Brand easing — all transitions |

**Convention:** Use `transition` with `duration-base` (180ms) and `ease-standard` for interactive states.

```html
class="transition duration-base ease-standard"
```

### 2.6 Opacity Modifiers

Used with Tailwind v4's opacity modifier syntax for background tints:

| Pattern                    | Example            | Usage                        |
|----------------------------|--------------------|------------------------------|
| `bg-{token}/{opacity}`     | `bg-primary/10`    | Status badge backgrounds     |
| `text-{token}/{opacity}`   | `text-white/80`    | Muted white on dark overlays |
| `border-{token}/{opacity}` | `border-danger/30` | Error state borders          |

Common opacity values: `/10` (badge bg), `/40` (overlay), `/60` (backdrop), `/80` (secondary text on dark), `/95` (glass surfaces).

---

## 3. Component System

### 3.1 Button

**Source:** `src/main/frontend/components/button.css`

Buttons use component classes defined in `@layer components`. These provide rich visual styling (gradients, shadows, hover lift) that cannot be expressed with utility classes alone.

#### Base Class

Every button starts with `.btn`:

```html
<button class="btn btn-primary">Save</button>
<a class="btn btn-secondary" th:href="@{/back}">Cancel</a>
```

The `.btn` base provides: inline-flex layout, gap, rounded-lg, font-medium, focus-visible ring, active scale-down, disabled styling, and transition.

#### Variants

| Variant     | Class           | When to use                                  |
|-------------|-----------------|----------------------------------------------|
| **Primary** | `.btn-primary`  | Main CTA on any page (create, save, confirm) |
| **Secondary**| `.btn-secondary`| Cancel, back, secondary actions              |
| **Accent**  | `.btn-accent`   | Premium actions, gold-themed CTAs            |
| **Danger**  | `.btn-danger`   | Destructive actions (delete, disable)        |
| **Ghost**   | `.btn-ghost`    | Minimal inline actions, toolbar buttons      |

#### Sizes

| Size        | Class    | Context                                                            |
|-------------|----------|--------------------------------------------------------------------|
| **Small**   | `.btn-sm`| Table actions, filter buttons, compact toolbars                    |
| **Default** | (none)   | Inline with forms, standard actions                                |
| **Large**   | `.btn-lg`| Page-level primary action (standalone)                             |

#### Rules

- Always use `.btn` + variant class — never build buttons from raw utility classes.
- Buttons have built-in hover lift (`translateY(-1px)`), active press (`scale(0.975)`), and focus ring.
- Disabled state (`.btn:disabled`) reduces opacity and removes pointer events automatically.
- Link-as-button: use `<a>` with same `.btn .btn-{variant}` classes.
- The old `rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white hover:opacity-90` pattern is **deprecated** — use `.btn .btn-primary` instead.

#### Examples

```html
<!-- Primary -->
<button class="btn btn-primary">Save Changes</button>

<!-- Primary (large, standalone) -->
<button class="btn btn-primary btn-lg">Create Account</button>

<!-- Secondary -->
<a class="btn btn-secondary" th:href="@{/admin/accounts}">Cancel</a>

<!-- Danger -->
<button class="btn btn-danger">Delete</button>

<!-- Accent -->
<button class="btn btn-accent">Upgrade to Premium</button>

<!-- Ghost (toolbar) -->
<button class="btn btn-ghost btn-sm">Edit</button>
```

### 3.2 Card / Panel

**Source:** `src/main/frontend/components/card.css`

Cards use component classes defined in `@layer components` for consistency. Every card starts with `.card` + optional variant.

#### Section Card (standard)

```html

<section class="card">
    <div class="card-header">
        <h2 class="card-title">Section Title</h2>
        <p class="card-description">Section description.</p>
    </div>
    <div class="card-body">
        ...
    </div>
</section>
```

#### Section Card (with table — needs overflow-hidden)

```html

<section class="overflow-hidden card">
    <div class="card-header">
        <h2 class="card-title">Table Title</h2>
    </div>
    <div class="overflow-x-auto">
        <table class="w-full">...</table>
    </div>
</section>
```

#### Compact Card (stat, metric tile)

```html

<div class="card-compact">
    <p class="text-sm text-muted-foreground">Label</p>
    <p class="mt-1 text-2xl font-bold">Value</p>
    <p class="mt-1 text-xs text-success">Change indicator</p>
</div>
```

#### Dashboard Panel

```html

<div class="card-panel">
    <h3 class="text-base font-semibold">Panel Title</h3>
    <p class="mt-1 text-sm text-muted-foreground">Content...</p>
</div>
```

#### Stat Card Variants

| Variant       | Class           | Use case                              |
|---------------|-----------------|---------------------------------------|
| Compact       | `card-compact`  | Default stat card                     |
| Panel         | `card-panel`    | Dashboard panels, table-less sections |
| Muted tile    | `card-muted`    | Metric tile inside panels             |
| Mini stat     | `card-mini`     | Compact mini-stat inside card grids   |

#### Elevated Card (toast, modal)

```html

<div class="card-elevated">
    <div class="flex gap-3 p-4">...</div>
</div>
```

#### Empty State

```html

<div class="card-empty">
    <h2 class="text-lg font-semibold">No Items Found</h2>
    <p class="mt-2 text-sm text-muted-foreground">Description of what to do next.</p>
</div>
```

#### Rules

- Top-level page sections always use `.card`.
- Inner/compact cards use `.card-compact` / `.card-panel`.
- Section header is always `.card-header` with `.card-title` and optional `.card-description`.
- Padding inside body is `.card-body`.
- For table shells, add `overflow-hidden` to `.card` to clip the border-radius.

### 3.3 Form Input

#### Text Input

```html
<input type="text"
       class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm
              outline-none focus:ring-2 focus:ring-primary"
       placeholder="Placeholder text">
```

#### Select

```html
<select class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm
               outline-none focus:ring-2 focus:ring-primary">
    <option>Option</option>
</select>
```

#### Textarea

```html
<textarea rows="4"
          class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm
                 outline-none focus:ring-2 focus:ring-primary"></textarea>
```

#### Input Label

```html
<label class="mb-2 block text-sm font-medium">Field Label</label>
```

#### Field Description

```html
<p class="mb-2 text-sm text-muted-foreground">Help text for this field.</p>
```

#### Error Message

```html
<p class="mt-1 text-sm text-danger">Error message</p>
```

#### Inline Error on Input

```html
<input th:classappend="${#fields.hasErrors('fieldName')} ? 'border-danger focus:ring-danger' : ''">
```

#### Rules

- All form inputs use `rounded-xl` (not `rounded-lg` or `rounded-2xl`).
- All inputs use `bg-background` (not `bg-surface`) to visually distinguish from the containing card.
- Always include `outline-none` + `focus:ring-2 focus:ring-primary`.
- Input height is `px-4 py-3` standard or `px-4 py-2` for compact contexts.
- Inside `grid grid-cols-2 gap-5 p-5`, each field is one grid cell.

#### Form Layout Pattern

```html

<section class="card">
    <div class="border-b border-border p-5">
        <h2 class="text-lg font-semibold">Section Title</h2>
        <p class="page-description">Section description.</p>
    </div>

    <div class="grid grid-cols-2 gap-5 p-5">
        <div>
            <label class="mb-2 block text-sm font-medium">Field</label>
            <input type="text" class="input w-full">
            <p th:if="${#fields.hasErrors('field')}"
               th:errors="*{field}"
               class="mt-1 text-sm text-danger">
            </p>
        </div>
        ...
    </div>
</section>
```

### 3.4 Table

```html

<section class="overflow-hidden card">
    <!-- optional header -->
    <div class="border-b border-border p-5">
        <h2 class="text-lg font-semibold">Title</h2>
    </div>

    <div class="overflow-x-auto">
        <table class="w-full">
            <thead class="bg-muted">
            <tr>
                <th class="px-5 py-4 text-left text-sm font-medium text-muted-foreground">Column</th>
                <th class="px-5 py-4 text-right text-sm font-medium text-muted-foreground">Actions</th>
            </tr>
            </thead>
            <tbody>
            <tr class="border-t border-border">
                <td class="px-5 py-4">
                    <p class="font-medium">Primary</p>
                    <p class="text-sm text-muted-foreground">Secondary</p>
                </td>
                <td class="px-5 py-4 text-right">
                    <a class="btn btn-secondary btn-sm">View</a>
                </td>
            </tr>
            <!-- Empty state -->
            <tr th:if="${#lists.isEmpty(items)}">
                <td colspan="4" class="px-5 py-10 text-center text-muted-foreground">
                    No items found.
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</section>
```

#### Rules

- Table shell: `overflow-hidden card`
- Header row: `bg-muted` with `text-sm font-medium text-muted-foreground` cells
- Data rows: `border-t border-border` separator
- Cell padding: `px-5 py-4`
- Primary text: `font-medium`; secondary: `text-sm text-muted-foreground`
- Action column: `text-right` alignment — use `btn btn-secondary btn-sm` for action buttons
- Empty state: `colspan={N} px-5 py-10 text-center text-muted-foreground`
- Always wrap table in `overflow-x-auto` for responsive horizontal scroll

### 3.5 Pill

**Source:** `src/main/frontend/components/pill.css`

Small inline indicator for trend changes, status, and labels. Follows the same `@layer components` pattern as button and card.

#### Base Variants

| Class            | When to use                                     |
|------------------|-------------------------------------------------|
| `.pill`          | Base class — raw pill shape, no color           |
| `.pill-success`  | Positive trends, confirmed, clean, active        |
| `.pill-warning`  | Pending, attention needed, limited               |
| `.pill-danger`   | Negative trends, failed, maintenance             |
| `.pill-muted`    | Neutral indicators, down trends, inactive        |

#### Examples

```html
<!-- Positive KPI pill -->
<p class="pill-success">+8.70% from last week</p>

<!-- Negative KPI pill -->
<p class="pill-muted">-1.06% from last week</p>

<!-- Status pill -->
<span class="pill-success">Active</span>
<span class="pill-danger">Disabled</span>
```

#### Room status variant

| Status              | Class              |
|---------------------|--------------------|
| VACANT / CLEAN      | `.pill-success`    |
| OCCUPIED / RESERVED | (use utility `bg-primary/10 text-primary`) |
| DIRTY               | `.pill-warning`    |
| MAINTENANCE         | `.pill-danger`     |
| Unknown             | `.pill-muted`      |

### 3.6 Feature / Amenity Tag

```html

<div class="rounded-full border border-border bg-background px-4 py-2 text-sm">
    <span>WiFi</span>
</div>
```

### 3.7 Activity / Timeline Item

```html

<div class="flex items-start gap-3">
    <div class="mt-2 h-2 w-2 rounded-full bg-primary"></div>
    <div>
        <p class="text-sm font-medium">Activity title</p>
        <p class="text-xs text-muted-foreground">Timestamp</p>
    </div>
</div>
```

Dot colors: `bg-primary` (default), `bg-success` (completed), `bg-warning` (attention).

### 3.8 User Pill (Sidebar top)

```html

<div class="flex items-center gap-3 rounded-xl border border-border bg-surface px-4 py-2 shadow-card">
    <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary font-semibold text-primary-foreground">
        JD
    </div>
    <div>
        <p class="text-sm font-medium text-foreground">John Doe</p>
        <p class="text-xs text-muted-foreground">Manager</p>
    </div>
</div>
```

### 3.9 Modal

```html

<div x-show="modalOpen"
     x-cloak
     class="fixed inset-0 z-modal flex items-center justify-center">

    <!-- Backdrop -->
    <div class="absolute inset-0 bg-black/60 backdrop-blur-sm"
         @click="modalOpen = false">
    </div>

    <!-- Panel -->
    <div x-show="modalOpen"
         x-transition:enter="transition ease-out duration-200"
         x-transition:enter-start="opacity-0 scale-95"
         x-transition:enter-end="opacity-100 scale-100"
         x-transition:leave="transition ease-in duration-150"
         x-transition:leave-start="opacity-100 scale-100"
         x-transition:leave-end="opacity-0 scale-95"
         @click.stop
         class="relative mx-4 w-full max-w-[36rem] rounded-2xl border border-border bg-surface p-6 shadow-float">

        <h2 class="text-lg font-semibold text-foreground">Title</h2>
        <p class="mt-2 text-sm text-muted-foreground">Content</p>

        <div class="mt-6 flex justify-end gap-3">
            <button @click="modalOpen = false"
                    class="btn btn-secondary">
                Cancel
            </button>
            <button type="submit"
                    class="btn btn-danger">
                Confirm
            </button>
        </div>
    </div>
</div>
```

Requires Alpine.js (`x-data`, `x-show`, `x-cloak`, `x-transition`). Include `[x-cloak] { display: none !important; }` in a `<style>` tag.

**Modal width:** Use `max-w-[--modal-width]` where `--modal-width` is `36rem` (standard), `24rem` (sm), or `48rem` (lg).

### 3.10 Toast Notification

The toast component is a Thymeleaf fragment at `fragments/components/toast.html`.

**Inclusion:**

```html

<div th:replace="~{fragments/components/toast :: toast}"></div>
```

**Triggering (controller):**

```java
model.addAttribute("success","Operation completed successfully");
model.addAttribute("error","Something went wrong");
```

**Structure (success variant):**

```html

<div class="overflow-hidden rounded-2xl border border-primary/30 bg-surface/95 shadow-soft ring-1 ring-primary/10 backdrop-blur-sm">
    <div class="flex gap-3 p-4">
        <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary shadow-sm">
            <!-- checkmark SVG -->
        </div>
        <div class="min-w-0 flex-1">
            <p class="mt-0.5 text-sm leading-relaxed text-muted-foreground">Message</p>
        </div>
        <button @click="show = false"
                class="rounded-lg px-2 py-1 text-sm leading-none text-muted-foreground transition hover:bg-muted hover:text-foreground">
            &times;
        </button>
    </div>
    <div class="h-1 bg-primary"></div>
</div>
```

**Error variant:** Replace `primary` with `danger`.

The toast uses Alpine.js for show/hide with auto-dismiss:

```html
x-data="{ show: true }"
x-init="setTimeout(() => show = false, 2000)"
```

### 3.11 Media Upload Box

```html

<div id="uploadBox"
     class="flex h-48 cursor-pointer flex-col items-center justify-center
            rounded-2xl border border-dashed border-border bg-background
            text-muted-foreground transition hover:border-primary">

    <p class="text-sm font-medium">Click, drag & drop, or paste image</p>
    <p class="text-xs text-muted-foreground">PNG, JPG, WEBP</p>
</div>

<input type="file"
       id="imageInput"
       name="image"
       accept="image/*"
       class="hidden">

<div id="previewContainer" class="mt-4 grid grid-cols-3 gap-3"></div>

<script th:src="@{/js/components/media-uploader.js}"></script>
<script>
    document.addEventListener("DOMContentLoaded", () => {
        Hospi.MediaUploader({
            uploadBox: "#uploadBox",
            input: "#imageInput",
            previewContainer: "#previewContainer"
        });
    });
</script>
```

With Alpine.js preview (alternative):

```html

<template x-if="preview">
    <img :src="preview" class="h-64 w-full rounded-xl border border-border object-cover">
</template>
<label x-data="{ preview: null }"
       @change="preview = URL.createObjectURL($event.target.files[0])">
```

### 3.12 Avatar & User Pill

**Source:** `src/main/frontend/components/avatar.css`

```html

<div class="user-pill">
    <div class="avatar">JD</div>
    <div>
        <p class="text-sm font-medium text-foreground">John Doe</p>
        <p class="text-xs text-muted-foreground">Manager</p>
    </div>
</div>
```

| Class         | Purpose                          |
|---------------|----------------------------------|
| `.avatar`     | Initials circle, 40×40, primary  |
| `.user-pill`  | User info bar with avatar + text |

### 3.13 Empty State

```html

<div class="rounded-2xl border border-border bg-surface p-10 text-center text-muted-foreground">
    No items found.
</div>

<!-- With heading -->
<div class="rounded-2xl border border-border bg-surface p-10 text-center">
    <h2 class="text-lg font-semibold">No Floors Configured</h2>
    <p class="mt-2 text-sm text-muted-foreground">Description of what to do next.</p>
</div>
```

---

## 4. Layout Patterns

### 4.1 Staff Shell

**Source:** `src/main/frontend/components/layout.css`

```html

<body class="staff-layout">

    <!-- SIDEBAR (replace role as needed) -->
    <div th:replace="~{fragments/{role}-sidebar :: sidebar}"></div>

    <!-- MAIN -->
    <main class="staff-page">

        <!-- TOAST -->
        <div th:replace="~{fragments/components/toast :: toast}"></div>

        <!-- PAGE HEADER -->
        <div class="staff-page-header">
            <div>
                <h1 class="staff-page-title">Page Title</h1>
                <p class="page-description">Page description.</p>
            </div>
            <div><!-- action buttons --></div>
        </div>

        <!-- CONTENT -->
        ...

    </main>

</body>
```

### 4.2 Stats Row

```html

<div class="grid grid-cols-4 gap-4">
    <div class="rounded-xl border border-border bg-surface p-4 shadow-card">
        <p class="text-sm text-muted-foreground">Label</p>
        <p class="mt-1 text-2xl font-bold">Value</p>
        <p class="mt-1 text-xs text-success">Trend indicator</p>
    </div>
    ...
</div>
```

### 4.3 Detail Grid (Two-Column Content)

```html

<div class="grid grid-cols-12 gap-6">
    <div class="col-span-8 flex flex-col gap-6">
        <!-- left panels -->
    </div>
    <div class="col-span-4 flex flex-col gap-6">
        <!-- side panels -->
    </div>
</div>
```

### 4.4 Form Action Bar

```html

<div class="flex items-center justify-end gap-3">
    <a th:href="@{/path}"
       class="btn btn-secondary btn-lg">
        Cancel
    </a>
    <button type="submit"
            class="btn btn-primary btn-lg">
        Save
    </button>
</div>
```

### 4.5 Filter Section

```html

<section class="card">
    <div class="border-b border-border p-5">
        <h2 class="text-lg font-semibold">Search & Filters</h2>
    </div>
    <div class="grid grid-cols-3 gap-5 p-5">
        <!-- filter fields -->
    </div>
</section>
```

### 4.6 Sidebar

Navigation items use the active/inactive pattern:

```html
<a th:href="@{/path}"
   class="flex items-center gap-3 rounded-lg p-2"
   th:classappend="${activeSidebar == 'KEY'}
       ? ' bg-primary/10 text-primary font-medium'
       : ' hover:bg-muted text-foreground'">

    <img th:src="@{/assets/icons/icon.svg}" alt="Label">
    Label
</a>
```

- Active state: `bg-primary/10 text-primary font-medium`
- Inactive state: `hover:bg-muted text-foreground`
- Width: `w-64 bg-surface border-r border-border`
- Logo area: `mb-8` with `h-9 w-9 rounded-lg bg-primary`
- Logout button at bottom: `mt-auto pt-4` with `btn btn-danger`

---

## 5. Icon System

Icons are inline SVG files in `src/main/resources/static/assets/icons/`. Reference them via Thymeleaf:

```html
<img th:src="@{/assets/icons/{name}(name=icon-name)}" alt="Description">
```

Available icons (11):

| File                     | Usage                                               |
|--------------------------|-----------------------------------------------------|
| `layout-grid.svg`        | Dashboard                                           |
| `user.svg`               | Staff accounts, user sections                       |
| `settings.svg`           | System configuration                                |
| `bell.svg`               | Notifications                                       |
| `bed-double.svg`         | Rooms, room types                                   |
| `calendar-check.svg`     | Reservations                                        |
| `building-2.svg`         | Hotel details                                       |
| `circle-dollar-sign.svg` | Revenues                                            |
| `list-todo.svg`          | Tasks                                               |
| `key-square.svg`         | Credentials                                         |
| `log-out.svg`            | Logout (used with `brightness-0 invert` on dark bg) |

---

## 6. JavaScript Integration

### Alpine.js

Loaded via CDN on pages that need interactivity:

```html

<script src="https://cdn.jsdelivr.net/npm/alpinejs@3/dist/cdn.min.js" defer></script>
```

Used for:

- Toast show/hide with auto-dismiss (`x-data`, `x-show`, `x-init`, `x-transition`)
- Modal open/close (`x-data`, `x-show`, `x-cloak`, `@click`)
- Image preview on upload (`@change`, `x-data`)

### Media Uploader

Custom module at `src/main/resources/static/js/components/media-uploader.js`. Provides `Hospi.MediaUploader()` with drag-and-drop, click-to-upload, and paste support.

---

## 7. Verification

Before committing styling work:

```bash
# 1. Rebuild the CSS
npm run css:build

# 2. Verify no inline styles exist
rg 'style=' src/main/resources/templates

# 3. Check that no raw hex colors are used in templates
rg '#[0-9A-Fa-f]{6}' src/main/resources/templates
```

- `rg 'style='` should return **no results** — inline styles are forbidden.
- `rg '#[0-9A-Fa-f]{6}'` should return **no results** in template files — always use semantic tokens.

### Token Audit

If you add a new color or token, update both the relevant `theme/*.css` file and this document. Every token must be:

1. Defined in a `@theme {}` block under `src/main/frontend/theme/`
2. Documented in this file
3. Used via utility classes in templates — never as raw hex

---

## 8. File Reference

| File                                      | Contents                            |
|-------------------------------------------|-------------------------------------|
| `src/main/frontend/app.css`               | Entry point — imports all sources   |
| `src/main/frontend/theme/colors.css`      | `@theme` color tokens               |
| `src/main/frontend/theme/typography.css`  | `@theme` font, text, tracking tokens|
| `src/main/frontend/theme/spacing.css`     | `@theme` radii, layout, z-index, breakpoints |
| `src/main/frontend/theme/shadows.css`     | `@theme` box-shadow tokens          |
| `src/main/frontend/theme/motion.css`      | `@theme` duration, easing tokens    |
| `src/main/frontend/components/layout.css` | `@layer components` layout classes  |
| `src/main/frontend/components/button.css` | `@layer components` button classes  |
| `src/main/frontend/components/card.css`   | `@layer components` card classes    |
| `src/main/frontend/components/pill.css`   | `@layer components` pill classes    |
| `src/main/frontend/components/avatar.css` | `@layer components` avatar classes  |
| `src/main/resources/static/css/app.css`   | **Generated** — never edit manually |

---

## 9. Migration Notes (tailwind.config.js → @theme)

| What changed                          | Before (`tailwind.config.js`)         | After (`theme/*.css`)                              |
|---------------------------------------|---------------------------------------|----------------------------------------------------|
| Config location                      | `tailwind.config.js`                  | `src/main/frontend/theme/*.css`                    |
| Token mechanism                      | JS object in `theme.extend`           | CSS `@theme {}` blocks                             |
| Build entry                          | `@config "../../../tailwind.config.js"` | `@import "tailwindcss"` then theme file imports |
| Colors                               | 12 raw + 14 semantic + info           | 7 raw + 11 semantic + 3 state (no `info`)          |
| `text-muted`                         | `#6B6258` (stone)                     | Replaced — use `text-muted-foreground` (`#aeaeae`) |
| `bg-muted` / `surface-warm`          | `surface-warm: #EEE7DA`               | `--color-muted: #EEE7DA`                           |
| `primary-dark` / `primary-soft`      | Separate tokens                       | Removed — use opacity modifiers (`primary/10`)     |
| `accent-soft` / `surface-muted`      | Separate tokens                       | Removed — use `muted` or opacity modifiers         |
| `subtle` / `surface-dark` / `text`   | Separate tokens                       | Removed                                            |
| `info`                               | `#2B6F8F`                             | Removed — use `primary` or `muted-foreground`      |
| `shadow-glow`                        | `0 0 0 3px rgba(199,164,93,0.34)`     | Removed — `.btn` has its own focus ring            |
| `shadow-inset`                       | Not present                           | Added                                              |
| `duration-DEFAULT`                   | 180ms                                 | Renamed to `duration-base`                         |
| `ease-hotel`                         | `cubic-bezier(0.22,1,0.36,1)`         | Renamed to `ease-standard`                         |
| `duration-fast`                      | Not present                           | Added (120ms)                                      |
| Letter spacing                       | `tracking-wide` / `tracking-widest`   | `tracking-brand` / `tracking-brand-wide`           |
| Line-heights on text sizes           | Explicit in config                    | Removed — Tailwind v4 auto-calculates              |
| Background gradients (`bg-hotel-*`)  | In config                             | Not migrated — define in `@layer utilities` if needed |
| Component classes                    | `.card-dark`, `.label-gold`, `.badge-suite` | Removed — replaced by `.btn` variants         |
| z-index                              | Not defined                           | Added as `--z-*` tokens                            |
| Breakpoints                          | Default Tailwind + custom             | Added explicit `xs` (480px)                        |
| Modal / drawer widths                | Not defined                           | Added as `--modal-*` / `--drawer-width`            |
| Content max-width                    | Not defined                           | Added `--layout-content-width: 72rem`              |

---

## 10. Design DOs and DON'Ts

### DO

- ✅ Use `.btn .btn-{variant}` for all buttons — not raw utility classes
- ✅ Use `.card` for section cards, `.card-compact` for stat cards
- ✅ Use `.card-header` + `.card-title` + `.card-description` for card headings
- ✅ Use `bg-background` for page, cards use `.card` component classes
- ✅ Use `text-muted-foreground` for secondary/description text
- ✅ Use `gap-5` inside card grids, `gap-4` for stat rows, `gap-6` for sections
- ✅ Use `rounded-2xl` for section cards, `rounded-xl` for inputs and compact cards
- ✅ Use `border border-border` on cards, tables, inputs
- ✅ Use `bg-{state}/10 text-{state}` pattern for status badges
- ✅ Put page header inside `staff-page-header` or `mb-6` container with `flex items-start justify-between`
- ✅ Use `z-modal`, `z-dropdown`, `z-toast` etc. for layering
- ✅ Use `duration-base` and `ease-standard` for interactive transitions
- ✅ Use horizontal padding `px-5` in card headers/bodies, `px-4` in compact cards

### DON'T

- ❌ Never use raw hex colors in templates
- ❌ Never use `text-green-600` or `bg-green-100` — use semantic tokens
- ❌ Never use inline `style` attributes
- ❌ Never edit `src/main/resources/static/css/app.css` directly
- ❌ Never use state colors (`success`, `danger`) for decorative/purpose elements
- ❌ Don't mix border radius levels — inputs are `rounded-xl`, containers are `rounded-2xl`
- ❌ Don't use `shadow-sm` or other Tailwind defaults — use `shadow-card`
- ❌ Don't build buttons from utility classes — use `.btn .btn-{variant}`
- ❌ Don't use `tracking-wide`/`tracking-widest` — use `tracking-brand`/`tracking-brand-wide`
