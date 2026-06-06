# Hospi Styling Guide

Hospi uses Tailwind CSS v4 as the single styling system. The app is a hotel booking and management product, so the
visual language should feel modern, calm, premium, and operationally clear.

## Setup

Tailwind source is in `src/main/frontend/tailwind.css`:

```css
@config "../../../tailwind.config.js";
@import "../node_modules/tailwindcss/dist/lib.d.mts";
```

The config file is `tailwind.config.js` at the project root. The `@config` directive loads it into Tailwind v4's
CSS-based config system.

### Content Scanning

Tailwind scans these paths for class usage:

| Path                                     | File types                            |
|------------------------------------------|---------------------------------------|
| `src/main/resources/templates/**/*.html` | Thymeleaf templates                   |
| `src/main/webapp/**/*.jsp`               | JSP partials (one file: `header.jsp`) |
| `src/main/webapp/**/*.html`              | Static HTML                           |
| `src/main/**/*.{js,ts}`                  | JavaScript and TypeScript             |

### Build Commands

| Command             | Output                                             |
|---------------------|----------------------------------------------------|
| `npm run css:build` | `src/main/resources/static/css/app.css` (minified) |
| `npm run css:watch` | Same output, watches for changes                   |

Both commands use Tailwind CLI v4 (`@tailwindcss/cli`). The output `app.css` is generated — never edit it manually.

A secondary copy exists at `src/main/webapp/css/app.css` (different content — rebuilt independently if needed).

## Palette

### Semantic Color Tokens

| Token           | Value     | Usage                                              |
|-----------------|-----------|----------------------------------------------------|
| `primary`       | `#1F6F5B` | Main CTAs, active navigation, key progress         |
| `primary-dark`  | `#123C35` | Dark primary surfaces and hover states             |
| `primary-soft`  | `#DDEDE7` | Soft labels, active backgrounds, subtle highlights |
| `accent`        | `#C7A45D` | Premium details, prices, small emphasis            |
| `accent-soft`   | `#F3E5C6` | Warm highlight backgrounds                         |
| `background`    | `#FBF8F1` | App/page background                                |
| `surface`       | `#FFFFFF` | Cards, tables, panels                              |
| `surface-muted` | `#F4EFE6` | Alternating bands and quiet sections               |
| `surface-warm`  | `#EEE7DA` | Warm secondary panels                              |
| `surface-dark`  | `#17211F` | Dark cards and hero overlays                       |
| `text`          | `#17211F` | Main text                                          |
| `muted`         | `#6B6258` | Secondary text                                     |
| `subtle`        | `#91887D` | Placeholders, hints, metadata                      |
| `border`        | `#DED5C7` | Dividers and input borders                         |

### Status Color Tokens

| Token     | Value     | Usage                                          |
|-----------|-----------|------------------------------------------------|
| `success` | `#2F7D4F` | Completed, available, confirmed                |
| `warning` | `#B7791F` | Pending, needs review, limited availability    |
| `danger`  | `#B84A3A` | Destructive actions, failed, occupied conflict |
| `info`    | `#2B6F8F` | Neutral informational state                    |

### Expressive Color Aliases

These map to the same values as above. Use them for one-off hotel moments or marketing content. Shared UI should prefer
semantic names above.

| Alias       | Maps to                                 |
|-------------|-----------------------------------------|
| `ink`       | `#17211F` (same as `text`)              |
| `forest`    | `#123C35` (same as `primary-dark`)      |
| `emerald`   | `#1F6F5B` (same as `primary`)           |
| `mint`      | `#DDEDE7` (same as `primary-soft`)      |
| `gold`      | `#C7A45D` (same as `accent`)            |
| `champagne` | `#F3E5C6` (same as `accent-soft`)       |
| `ivory`     | `#FBF8F1` (same as `background`)        |
| `porcelain` | `#FFFFFF` (same as `surface`)           |
| `stone`     | `#6B6258` (same as `muted`)             |
| `linen`     | `#EEE7DA` (same as `surface-warm`)      |
| `clay`      | `#A14F35` (no semantic alias — use raw) |

## Font Families

| Family         | Value                                                       | Usage                                 |
|----------------|-------------------------------------------------------------|---------------------------------------|
| `font-display` | `"Cormorant Garamond", Georgia, serif`                      | Headings, hero text, premium sections |
| `font-sans`    | `"DM Sans", ui-sans-serif, system-ui, sans-serif`           | Body text, UI labels, data tables     |
| `font-mono`    | `"JetBrains Mono", ui-monospace, SFMono-Regular, monospace` | Code, rates, numbers in data          |

## Border Radius Tokens

| Token               | Value      |
|---------------------|------------|
| `rounded-sm`        | `0.375rem` |
| `rounded` (DEFAULT) | `0.5rem`   |
| `rounded-md`        | `0.625rem` |
| `rounded-lg`        | `0.75rem`  |
| `rounded-xl`        | `1rem`     |
| `rounded-2xl`       | `1.25rem`  |

`rounded-xl` is the standard card/panel radius. `rounded-2xl` is used for elevated containers like section cards and
modals.

## Box Shadow Tokens

| Token          | Value                                | Usage                          |
|----------------|--------------------------------------|--------------------------------|
| `shadow-card`  | `0 12px 32px rgba(23, 33, 31, 0.08)` | Default card shadow            |
| `shadow-soft`  | `0 18px 48px rgba(23, 33, 31, 0.10)` | Elevated panels, modals        |
| `shadow-float` | `0 24px 64px rgba(23, 33, 31, 0.14)` | Floating elements, toasts      |
| `shadow-glow`  | `0 0 0 3px rgba(199, 164, 93, 0.34)` | Focus glow for accent elements |

## Background Image Utilities

| Utility            | Gradient                              |
|--------------------|---------------------------------------|
| `bg-hotel-dark`    | `135deg, #17211F → #123C35 → #1F6F5B` |
| `bg-hotel-gold`    | `135deg, #C7A45D → #F3E5C6`           |
| `bg-hotel-surface` | `180deg, #FFFFFF → #FBF8F1`           |

These are defined in `tailwind.config.js` as `backgroundImage` but are **not used** in any current template. They exist
for future public-facing or hero sections.

## Component Classes

These utility component classes are registered in `tailwind.config.js` via the `addComponents` plugin. They are also *
*unused** in current templates — available for future marketing or hero sections:

| Class          | Purpose                              |
|----------------|--------------------------------------|
| `.card-dark`   | Dark gradient card with float shadow |
| `.label-gold`  | Gold display-font uppercase label    |
| `.badge-suite` | Accent-toned uppercase badge         |

## Spacing Tokens

| Token     | Value    | Usage                      |
|-----------|----------|----------------------------|
| `sidebar` | `16rem`  | Sidebar width              |
| `card`    | `1.5rem` | Card padding/gap           |
| `section` | `3rem`   | Section spacing            |
| `prose`   | `42rem`  | Max-width for text content |

## Tailwind Rules

- Use Tailwind utilities and project tokens — no handwritten page CSS.
- No inline `style` attributes in templates.
- No external CSS frameworks alongside Tailwind.
- Service only `src/main/resources/static/css/app.css` from templates.
- Rebuild with `npm run css:build` after changing templates, config, or Tailwind input.

## Token Usage Patterns

### Defaults

| Element           | Classes                                                                   |
|-------------------|---------------------------------------------------------------------------|
| Page body         | `bg-background text-text`                                                 |
| Card / panel      | `rounded-2xl border border-border bg-surface shadow-card`                 |
| Card (compact)    | `rounded-xl border border-border bg-surface p-4 shadow-card`              |
| Muted section     | `bg-surface-muted`                                                        |
| Table shell       | `overflow-hidden rounded-2xl border border-border bg-surface shadow-card` |
| Table header cell | `px-5 py-4 text-left text-sm font-medium text-muted`                      |

### Action Defaults

| Element          | Classes                                                                                                               |
|------------------|-----------------------------------------------------------------------------------------------------------------------|
| Primary button   | `rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white shadow-card hover:opacity-90`                         |
| Secondary button | `rounded-xl border border-border px-4 py-2 text-sm font-medium text-text hover:bg-background`                         |
| Danger button    | `rounded-xl bg-danger px-4 py-2 text-sm font-medium text-white hover:opacity-90`                                      |
| Input            | `w-full rounded-xl border border-border bg-background px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary` |

### Label / Badge Defaults

| Element            | Classes                                                                        |
|--------------------|--------------------------------------------------------------------------------|
| Badge (status)     | `rounded-full bg-primary-soft px-2 py-1 text-xs font-medium text-primary-dark` |
| Secondary text     | `text-muted`                                                                   |
| Placeholder / help | `text-subtle`                                                                  |
| Accent label       | `text-accent`                                                                  |
| Focus ring         | `focus:ring-2 focus:ring-primary`                                              |

## Layout

- Staff screens: `flex min-h-screen` with `w-64` sidebar.
- Page content: `flex-1 bg-background p-6`.
- Section descriptions: `mt-1 text-sm text-muted`.
- Dense operational pages use tables, compact cards (`p-4`), and clear headers.
- Public hotel pages can use richer imagery, larger headings, and `bg-hotel-dark` / `bg-hotel-surface`.

## Verification

```bash
# Rebuild the output CSS
npm run css:build

# Check that no inline styles exist in templates
rg 'style=' src/main/resources/templates
```

The second command should return no results. If it does, the inline `style` attribute needs to be replaced with a
Tailwind utility.

## Unused Config: Notes for Future Work

The following tokens and component classes exist in `tailwind.config.js` but are not used in any current template. They
were designed for future public-facing or hero sections:

- `bg-hotel-dark`, `bg-hotel-gold`, `bg-hotel-surface` (backgroundImage)
- `.card-dark`, `.label-gold`, `.badge-suite` (component classes)
- `shadow-float`, `shadow-glow` (box shadows)
- `font-display` (Cormorant Garamond — only `font-sans` is in use)
- `clay` color (`#A14F35`)
