# Audit Logging — What Gets Logged

Every action is logged via `AuditService.log(staffId, staffName, action, entityType, entityId, details)`.
Staff identity comes from `@AuthenticationPrincipal AccountPrincipal` in controllers.

## Entity: `RESERVATION` (12 call sites)

| Action | When |
|--------|------|
| `CHECKIN` | Guest checks in |
| `CHECKOUT` | Checkout completed (payment + vacate rooms) |
| `PAYMENT` | Payment confirmed / refund processed / offline refund |
| `ASSIGN_ROOM` | Room assigned to a reservation |
| `REMOVE_ROOM` | Room assignment removed |
| `SWAP_ROOM` | Room type swapped on a checked-in stay |
| `EXTEND_STAY` | Stay extended by N days |
| `UPDATE` | Reservation cancelled |

## Entity: `STAYING_GUEST` (5 call sites)

| Action | When |
|--------|------|
| `CREATE` | Guest added to a reservation |
| `UPDATE` | Guest details edited |
| `DELETE` | Guest removed from a reservation |

## Entity: `ROOM` (2 call sites)

| Action | When |
|--------|------|
| `CLEAN_ROOM` / `DIRTY_ROOM` / `MAINTENANCE_ROOM` | Room condition status changed |

## Entity: `ACCOUNT` (3 call sites)

| Action | When |
|--------|------|
| `CREATE` | New staff account created |
| `UPDATE` | Staff account edited |
| `DELETE` | Staff account soft-deleted |

## Entity: `CREDENTIAL` (1 call site)

| Action | When |
|--------|------|
| `CHANGE_PASSWORD` | Staff changes own password |

## Entity: `SYSTEM_CONFIG` (1 call site)

| Action | When |
|--------|------|
| `UPDATE` | System configuration updated |
