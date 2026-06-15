# Admin Seeder Improvements

**Status:** ready-for-human
**Priority:** Low
**Created:** 2026-06-15
**Depends on:** Admin seeder (completed with hardcoded defaults)

## Current State

The `AdminSeeder` uses hardcoded defaults:
- username: `admin`
- email: `admin@moviebookingapp.com`
- password: `Admin123`

## Future Improvements

- [ ] Read admin credentials from environment variables (`ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`) via `@Value` or a properties class
- [ ] Add `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` to `.env.example`
- [ ] Require `ADMIN_PASSWORD` in production (no default)
- [ ] Add a password change endpoint (`PUT /api/v1/auth/password`)
- [ ] Log a warning on startup if admin still uses the default password
