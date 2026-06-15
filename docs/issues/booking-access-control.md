# Booking Access Control with Auth

**Status:** ready-for-human  
**Priority:** High  
**Created:** 2026-06-08  
**Depends on:** Auth feature (completed)

## Problem

Now that authentication is implemented (JWT + roles), the booking endpoints need access control. Currently:

- All booking endpoints are `permitAll()` — correct for guest access, but no ownership checks exist
- `userId` in `BookingReqDto` comes from the request body — a logged-in user could pass another user's ID
- Any authenticated user can view/confirm/cancel any booking by ID
- `X-Guest-Booking-Token` header is required on all read/confirm/cancel endpoints, even for authenticated users

## Acceptance Criteria

- [ ] Authenticated users' `userId` is derived from the JWT, not the request body
- [ ] Registered users can only view/confirm/cancel their own bookings (ownership check)
- [ ] Admins can view/confirm/cancel any booking
- [ ] Guest flow continues to work: `permitAll()` + `guestAccessToken` verification
- [ ] `X-Guest-Booking-Token` header is optional for authenticated users
- [ ] Existing booking tests updated to cover both paths (guest and authenticated)
- [ ] New tests for ownership enforcement (user A cannot access user B's booking)

## Design Notes

See full analysis: `booking_access_control_analysis.md` in conversation `e8a0dc85-d2fe-4aed-a19f-1ffad15c651f`

### Dual-path access model:

| Caller | Create | View/Confirm/Cancel |
|---|---|---|
| Guest (no JWT) | `userId = null`, contact details required | `X-Guest-Booking-Token` required |
| Customer (JWT) | `userId` from JWT automatically | JWT ownership: `booking.user.id == jwt userId` |
| Admin (JWT) | Same as customer | Bypass ownership — can access any booking |

### Files to change:
- `BookingController` — inject `@AuthenticationPrincipal`, make `X-Guest-Booking-Token` optional
- `BookingService` — add ownership check logic, extract userId from auth context
- `BookingReqDto` — `userId` field ignored for authenticated users
- `BookingControllerApiContractTest` — update for dual-path
- `BookingServiceTest` — add ownership tests
- `SecurityEnforcementTest` — verify booking ownership enforcement

### Open questions:
- Should `GET /api/v1/me/bookings` be added for "list my bookings"?
- Should guest booking lookup by email (`GET /api/v1/bookings?email=...`) be added? (paused until guest retrieval design is clear)
- Should `guestAccessToken` still be generated for registered user bookings? (recommended: yes, for uniform storage)
