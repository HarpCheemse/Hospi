# Floating Room Details Modal

Date: 2026-07-19

## Problem

Guests on the booking rooms page see only the room name, price, availability, and max occupancy. They cannot preview the full room details (description, features, all pictures, bed type, category, tier) without leaving the booking flow.

## Solution

Add a floating modal overlay triggered by clicking any room image. Keep the guest on the same page — no redirect, no navigation.

## Changes

### 1. Data Layer — `RoomTypeAvailabilityView.java`

Add three fields to the existing record:

| Field | Source | Type |
|---|---|---|
| `description` | `roomType.getDescription()` | `String` |
| `category` | `roomType.getCategory()` | `RoomCategory` (SINGLE, DOUBLE, FAMILY, SUITE) |
| `tier` | `roomType.getTier()` | `RoomTier` (BASIC, SUPERIOR, DELUXE) |

The existing constructor `RoomTypeAvailabilityView(RoomTypeAvailability)` already has access to the full `RoomType` entity — just plumb the three new getters through.

### 2. Template — `rooms.html`

#### 2a. JS data bridge

Extend each `ROOMS_DATA` object with:

```js
{
  description: "...",
  features: "...",
  bedType: "KING",
  category: "DOUBLE",
  tier: "SUPERIOR",
  pictures: [{id: 1}, {id: 2}, ...]  // replace single pictureId with array
}
```

**Card image binding update:** The existing `<img x-bind:src="room.pictureId ? ... : ...">` must change to `<img x-bind:src="room.pictures.length ? '/room-type-picture/' + room.pictures[0].id : '/images/hotel2.jpg'">` to use the new array structure.

#### 2b. Clickable image

Wrap the existing `<img>` in a `<button type="button" @click="openModal(room)">` to trigger the modal.

#### 2c. Alpine.js state additions

Add to the `<form>` `x-data`:

| State | Type | Default | Purpose |
|---|---|---|---|
| `modalRoom` | Object or null | `null` | Currently displayed room |
| `modalPicIdx` | Number | `0` | Index into `modalRoom.pictures` |
| `openModal(room)` | Method | — | Set `modalRoom`, reset `modalPicIdx = 0` |
| `closeModal()` | Method | — | Set `modalRoom = null` |
| `nextPic()` | Method | — | Increment `modalPicIdx`, wrap to 0 if past end |
| `prevPic()` | Method | — | Decrement `modalPicIdx`, wrap to last if < 0 |
| `allPictures(room)` | Method | — | Return pictures array or fallback single-element with null id |

#### 2d. Modal markup (inserted after the right sidebar, inside `<form>`)

**Backdrop:**
- `class="fixed inset-0 z-50 flex items-center justify-center"`
- `bg-black/60 backdrop-blur-sm`
- `@click="closeModal()"` closes on backdrop click
- `x-show="modalRoom !== null"`, `x-cloak`
- `x-transition:enter` / `x-transition:leave` for fade

**Panel (centered, max-w-2xl):**
- `@click.stop` prevents backdrop close on panel content click
- `class="relative mx-4 w-full max-w-2xl rounded-2xl border border-border bg-surface p-0 shadow-2xl overflow-hidden"`

**Close button:**
- Absolute top-right, `@click="closeModal()"` with X icon

**Image gallery area:**
- Full-width image, `h-72 md:h-96` via `object-cover`
- Source bound to `allPictures(modalRoom)[modalPicIdx].id ? '/room-type-picture/' + id : '/images/hotel2.jpg'`
- Previous / Next arrow buttons (chevron SVGs) overlaid left and right on the image
  - `@click.stop="prevPic()"` / `@click.stop="nextPic()"`
  - Show only if more than 1 picture
- Dot indicators row below the image
  - Loop over all pictures, `@click.stop="modalPicIdx = index"`
  - Current dot highlighted with `bg-primary`, others `bg-border`

**Details area below image:**
- **Header:** Room name (large font-display), category badge (pill), tier badge (pill)
- **Price:** `/night` label in accent color
- **Info grid:** 4 columns — bed type, max occupancy, area, price per night (each with an icon and label)
- **Description:** full text paragraph, muted text
- **Features:** split `features` string by common delimiters (commas, semicolons), render each as a pill/badge. Guard: skip section if `features` is empty/null
- **Action button:** "Close" secondary button at bottom

### 3. Tests

**BookingFlowControllerTest `showRooms_shouldRender`:** Update content assertion to verify the modal markup is present and contains at least one new data field from the extended bridge (e.g., `containsString("description")` or a known feature text if using seeded data).

No new test classes needed — no new endpoints, services, or controllers.

## Non-Goals

- No new server endpoints (Thymeleaf/JS bridge is sufficient)
- No REST API — not introducing fetch-based data loading for this feature
- No gallery page or separate room details page
- No video tours, floor plans, or 360° views

## What Doesn't Change

- `BookingFlowService`, `RoomAvailabilityService` — zero changes
- Security, routing, session logic
- Image storage and serving pattern (BYTEA columns, `/room-type-picture/{id}` endpoints)
- Existing sidebar and room selection UI
- Form submission logic
