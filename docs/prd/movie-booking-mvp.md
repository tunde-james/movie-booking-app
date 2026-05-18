# Movie Booking App MVP PRD

Last updated: 2026-05-18  
Status: Draft

## Problem Statement

Build a Java Spring Boot movie booking backend that lets admins manage the movie catalogue and show schedule, while customers can browse movies, view showtimes, and create ticket bookings.

The MVP should establish clean domain rules, API contracts, persistence behavior, and tests before adding payments, seat maps, notifications, AI assistant features, or full authentication.

## Goals

- Let admins manage movies, cinemas, auditoriums, and shows.
- Let public users browse visible movies and showtimes.
- Let customers create, confirm, cancel, and view bookings.
- Use quantity-based bookings, not seat selection.
- Snapshot prices on bookings.
- Use soft delete for persisted domain entities.
- Use Problem Details for API errors.
- Keep the design testable and ready for future authentication and AI features.

## Non-Goals

- Payments.
- Seat maps or individual seat selection.
- Notifications.
- AI assistant workflows.
- Full authentication/authorization as the first implementation step.
- Multi-country timezone support.

## Users And Roles

- Public users can list movies, view movie details, and list showtimes.
- Customers can create, confirm, cancel, and view their own bookings.
- Admins can create, update, and delete movies, cinemas, auditoriums, and shows.

## Core Domain

- Movie: a film that can be scheduled for shows.
- Cinema: a physical venue/building.
- Auditorium: a room inside a cinema where shows happen.
- Show: a scheduled screening of a movie in an auditorium.
- Booking: a customer's reservation request for tickets to a show.

## Functional Requirements

### Movies

- Movies contain title, description, genre, duration, release date, language, rating, status, and poster URL.
- Movie statuses are `DRAFT`, `COMING_SOON`, `NOW_SHOWING`, and `ARCHIVED`.
- Public users see only `COMING_SOON` and `NOW_SHOWING`.
- Movie titles are not globally unique.
- Exact duplicates are rejected only when `title + releaseDate + language` match.

### Cinemas And Auditoriums

- Cinemas contain name, address, and city.
- Auditoriums belong to a cinema.
- Auditoriums contain name/auditorium number, type, and capacity.
- Auditorium types are `STANDARD`, `IMAX`, and `VIP`.
- `totalAuditoriums` is derived by counting auditoriums.

### Shows

- Shows belong to a movie and auditorium.
- Shows contain start time, end time, total capacity, available capacity, price per ticket, and status.
- Show statuses are `SCHEDULED`, `CANCELLED`, and `COMPLETED`.
- `SOLD_OUT` is derived from `availableCapacity == 0`.
- Shows snapshot `totalCapacity` and `pricePerTicket` when created.
- Shows cannot overlap in the same auditorium.
- A 15-minute cleanup buffer is required between shows.

### Bookings

- Bookings start as `PENDING`.
- Confirming a booking changes it to `CONFIRMED` and reduces show availability.
- Cancelling a confirmed booking restores show availability.
- Bookings cannot be cancelled after show start time.
- Booking price is snapshotted when the pending booking is created.
- Booking statuses are `PENDING`, `CONFIRMED`, and `CANCELLED`.

## API Decisions

- Movie list/search: `GET /api/v1/movies`
- Movie detail: `GET /api/v1/movies/{movieId}`
- Show list/search: `GET /api/v1/shows`
- Show detail: `GET /api/v1/shows/{showId}`
- Filtering uses query parameters.
- Create operations return `201 Created` with a `Location` header.
- Update operations use `PUT` and return `200 OK`.
- Delete operations use soft delete and return `204 No Content`.
- `PATCH` is out of scope for v1.

## Error Handling

Use Problem Details, RFC 9457/RFC 7807 style.

Status mapping:

- `400`: validation error or invalid state transition.
- `401`: authentication required.
- `403`: authenticated user lacks permission.
- `404`: resource missing or soft-deleted.
- `409`: duplicate movie, schedule conflict, insufficient capacity, or blocked delete.

## Persistence Decisions

All persisted domain entities use:

- `id`
- `createdAt`
- `updatedAt`
- `deleted`

Deletes are soft deletes.

Add indexes for known query paths:

- movie duplicate checks and public search
- auditorium lookup by cinema
- show search and scheduling conflict checks
- booking lookup by customer, show, and status

## Testing Strategy

Use TDD per vertical slice.

Start with:

1. API foundation.
2. Movie catalogue.
3. Cinema and auditorium management.
4. Show scheduling.
5. Pending booking creation.
6. Booking confirmation.
7. Booking cancellation and viewing.
8. Authentication and authorization.

## Open Questions

- Should soft-deleted records be restorable by admins?
- Should uniqueness checks ignore soft-deleted records?
- Should authentication use JWT, sessions, or another approach?
- Should show completion be automatic or admin-triggered?