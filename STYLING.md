# Hospi Styling Guide

This project uses Tailwind utilities as the only styling system.

## Rules

- Do not add handwritten page, component, layout, or base CSS files.
- Do not add inline `style` attributes in JSP files.
- Do not use external CSS frameworks alongside Tailwind.
- Serve only `src/main/webapp/css/app.css` in JSP pages.
- Treat `src/main/webapp/css/app.css` as generated output. Do not edit it by hand.
- Put Tailwind source in `src/main/frontend/tailwind.css`.
- Rebuild CSS with `npm run css:build` after changing JSP classes, Tailwind config, or Tailwind input.

## Tokens

Use semantic project tokens from `tailwind.config.js` before raw palette utilities.

- Page background: `bg-background`
- Main surface: `bg-surface`
- Muted surface: `bg-surface-muted`
- Primary action: `bg-primary text-white`
- Secondary text: `text-muted`
- Main text: `text-text`
- Borders: `border-border`
- Focus ring: `focus:ring-2 focus:ring-primary`
- Card shadow: `shadow-card`

Use status colors only for state:

- Success: `bg-green-100 text-green-700`
- Warning: `bg-yellow-100 text-yellow-700`
- Danger: `bg-red-100 text-red-700`
- Info: `bg-blue-100 text-blue-700`

## Layout

- Staff screens use `flex min-h-screen`.
- Sidebar width is `w-64`.
- Page content uses `flex-1 bg-background p-6`.
- Main content spacing starts with `space-y-6` or explicit `mb-6`.
- Dense operational pages should use tables, compact cards, and clear section headers.

## Components

Primary button:

```html
class="rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white shadow-card hover:opacity-90"
```

Secondary button:

```html
class="rounded-xl border border-border px-4 py-2 text-sm font-medium text-text hover:bg-background"
```

Danger button:

```html
class="rounded-xl bg-red-500 px-4 py-2 text-sm font-medium text-white hover:opacity-90"
```

Input:

```html
class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary"
```

Card:

```html
class="rounded-xl border border-border bg-surface p-5 shadow-card"
```

Table shell:

```html
class="overflow-hidden rounded-xl border border-border bg-surface shadow-card"
```

Badge:

```html
class="rounded-full px-2 py-1 text-xs font-medium"
```

## Consistency Checks

Before committing styling changes:

- Search for new CSS files: `rg --files src/main/webapp | rg "\\.css$"`
- Confirm only `src/main/webapp/css/app.css` is served by JSPs.
- Run `npm run css:build`.
- Run `mvn clean package`.
