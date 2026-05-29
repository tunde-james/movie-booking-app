# Issue Tracker

This project does not currently have a configured external issue tracker.

Until configured otherwise, agents should propose issues in chat or local markdown only. Do not create GitHub, Jira, Linear, or other external issues unless the user asks.

## Follow-up: Booking Management

Before implementing booking creation/confirmation/cancellation, revisit the show-management booking guard added in the "Protect show management from booking conflicts" commit.

Review:
- `BookingRepository`
- `V6__create_users_and_bookings_tables.sql`
- `ShowService.ensureShowHasNoActiveBookings`
- show update/delete behavior when bookings exist

Explain the flow before building on it.