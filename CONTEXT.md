# Movie Booking App Context
Last updated: 2026-05-18

This project is a Java Spring Boot movie booking application.

## Product Scope

V1 lets:
- admins create movies, cinemas, auditoriums, and shows
- customers browse movies
- customers view showtimes
- customers create, confirm, and cancel bookings

Out of scope for v1:
- payments
- seat maps
- AI assistant
- notifications
- full authentication/authorization implementation until later in the build sequence

## Core Language

**Movie**: A film that can be listed and scheduled for shows.

**Cinema**: A physical venue/building where movies are shown.

**Auditorium**: A room inside a cinema where shows happen.

**Show**: A scheduled screening of a movie in an auditorium.

**Booking**: A customer's reservation request for tickets to a show.

**Admin**: A user who can manage movies, cinemas, auditoriums, and shows.

**Customer**: A user who can browse movies, view showtimes, and create bookings.

## Shared Entity Fields

Persisted domain entities use shared audit and lifecycle fields:
- id
- createdAt
- updatedAt
- deleted

`id` is the database identity for persisted resources.

`createdAt` records when the entity was created.

`updatedAt` records when the entity was last changed.

`deleted` marks whether the entity has been soft-deleted.

Delete operations should soft-delete records instead of physically removing them from the database.

## Relationships

- A Cinema has many Auditoriums.
- An Auditorium belongs to one Cinema.
- A Movie has many Shows.
- A Show belongs to one Movie.
- A Show happens in one Auditorium.
- A Show's Cinema is derived through `Show -> Auditorium -> Cinema`.
- A Booking belongs to one Customer.
- A Booking belongs to one Show.

## Access Model

Public users can:
- list movies
- view movie details
- list showtimes

Customers can:
- create bookings
- confirm their own pending bookings
- cancel their own bookings before show start time
- view their own bookings

Admins can:
- create, update, and delete movies
- create, update, and delete cinemas
- create, update, and delete auditoriums
- create, update, and delete shows

Authentication and authorization will be implemented after the core workflows are shaped.

## Movie Model

V1 Movie contains:
- title
- description
- genre
- durationInMinutes
- releaseDate
- language
- rating
- status
- posterUrl

V1 Movie statuses:
- `DRAFT`
- `COMING_SOON`
- `NOW_SHOWING`
- `ARCHIVED`

Public users can see only:
- `COMING_SOON`
- `NOW_SHOWING`

Movie titles are not globally unique. Movie identity is the database id.

For v1, the app rejects only exact duplicate movies with the same:
- title
- releaseDate
- language

Later, duplicate detection may use external identifiers such as TMDB or IMDb ids.

## Cinema And Auditorium Model

V1 Cinema contains:
- name
- address
- city

V1 Auditorium contains:
- cinema
- name or auditoriumNumber
- type
- capacity

V1 Auditorium types:
- `STANDARD`
- `IMAX`
- `VIP`

`totalAuditoriums` is derived by counting auditoriums for a cinema, not stored on Cinema.

## Show Model

V1 Show contains:
- movie
- auditorium
- startTime
- endTime
- totalCapacity
- availableCapacity
- pricePerTicket
- status

V1 persisted Show statuses:
- `SCHEDULED`
- `CANCELLED`
- `COMPLETED`

`SOLD_OUT` is derived when `availableCapacity == 0`.

`totalCapacity` and `pricePerTicket` are snapshotted on the show when it is created.

A show is bookable when:
- status is `SCHEDULED`
- `startTime` is in the future
- `availableCapacity > 0`

Admins can cancel a show only when it has no `CONFIRMED` bookings.

Admins can update major show details only while the show has no bookings.

## Show Updates

Admins can update major show details only while the show has no bookings.

If a show has any `PENDING` or `CONFIRMED` bookings, admins cannot change:
- movie
- auditorium
- startTime
- endTime
- totalCapacity
- pricePerTicket

This protects customer expectations and avoids mismatched booking snapshots.

## Show Scheduling

An auditorium cannot have overlapping active shows.

A show conflicts with another active show in the same auditorium if their time ranges overlap.

A 15-minute cleanup buffer is required between shows in the same auditorium.

Example:
- A show ending at `12:00` allows the next show to start at `12:15`
- A show ending at `12:00` rejects another show starting at `12:10`

Cancelled shows do not block scheduling.

## Booking Model

V1 booking statuses:
- `PENDING`
- `CONFIRMED`
- `CANCELLED`

A customer creates a booking as `PENDING`.

A separate confirmation action changes the booking to `CONFIRMED` and reduces show availability.

A cancellation action changes the booking to `CANCELLED`. If the booking was already `CONFIRMED`, cancellation restores show availability.

Booking contains:
- customer
- show
- ticketQuantity
- unitPrice
- totalPrice
- status

Pricing is snapshotted when the booking is created as `PENDING`.

`unitPrice = Show.pricePerTicket`

`totalPrice = unitPrice * ticketQuantity`

When a `PENDING` booking is created:
- the show must exist
- the show must be bookable
- `ticketQuantity` must be positive
- `ticketQuantity <= availableCapacity`
- show capacity is not reduced

When a booking is confirmed:
- the booking must be `PENDING`
- the show must still be bookable
- `ticketQuantity <= availableCapacity`
- show capacity is reduced by `ticketQuantity`
- booking becomes `CONFIRMED`

When a `PENDING` booking is cancelled:
- booking becomes `CANCELLED`
- show capacity does not change

When a `CONFIRMED` booking is cancelled:
- booking becomes `CANCELLED`
- show `availableCapacity` increases by `ticketQuantity`

Bookings cannot be cancelled after the show start time in v1.

## Search And API Shape

Movie list/search:

`GET /api/v1/movies`

Optional filters:
- `title`
- `genre`
- `language`
- `status`

Movie identity:

`GET /api/v1/movies/{movieId}`

Show list/search:

`GET /api/v1/shows`

Optional filters:
- `movieId`
- `cinemaId`
- `auditoriumId`
- `date`
- `status`

Show identity:

`GET /api/v1/shows/{showId}`

Filtering and search use query parameters, not separate endpoints.

## Delete Rules

Delete operations use soft delete.

V1 blocks deleting:
- a Movie if it has active Shows
- a Cinema if it has active Auditoriums or active Shows through those Auditoriums
- an Auditorium if it has active Shows
- a Show if it has `PENDING` or `CONFIRMED` bookings
- a Customer/User if they have `PENDING` or `CONFIRMED` bookings

An active Show means:
- status is `SCHEDULED`
- not soft-deleted

## API Error Format

V1 uses Problem Details for HTTP APIs, following RFC 9457/RFC 7807 style.

Standard error fields:
- `type`
- `title`
- `status`
- `detail`
- `instance`

Validation errors may include an `errors` extension array with field-level messages.

Common status mapping:
- `400 Bad Request`: validation errors or invalid state transitions
- `401 Unauthorized`: authentication required
- `403 Forbidden`: authenticated user lacks permission
- `404 Not Found`: resource does not exist or is soft-deleted
- `409 Conflict`: duplicate movie, schedule conflict, insufficient capacity, or blocked delete

## Delete API Behavior

Successful delete operations return `204 No Content`.

Because deletes are soft deletes internally, the API still treats the resource as deleted after success.

Blocked deletes return `409 Conflict`.

Missing or already deleted resources return `404 Not Found`.

## Create And Update API Behavior

Successful create operations return:
- `201 Created`
- `Location` header pointing to the created resource
- response body containing the created resource

Successful update operations return:
- `200 OK`
- response body containing the updated resource

## Update API Behavior

V1 uses `PUT` for full resource updates.

`PATCH` is out of scope for v1.

Create, update, and delete use:
- `POST` for create
- `PUT` for full update
- `DELETE` for soft delete

## Time And Time Zone Strategy

V1 uses ISO-8601 timestamps for API date-time fields.

Show `startTime` and `endTime` use `OffsetDateTime`.

Default business timezone is `Africa/Lagos` / `UTC+01:00`.

Example show time:

`2026-06-01T18:30:00+01:00`

If the app later supports cinemas across multiple time zones, add a `timeZone` field to Cinema.

## Open Questions

- Should show scheduling prevent overlapping shows in the same auditorium?
- What error response format should the API use?
- Should delete operations be soft delete or hard delete?
- Which test style should be used for the first vertical slice?
- Should soft-deleted records be restorable by admins?
- Should uniqueness checks ignore soft-deleted records?
- What delete operations should be blocked when active child records exist?