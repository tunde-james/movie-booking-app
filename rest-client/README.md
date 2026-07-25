# Movie Booking App REST Client

These files are for the VS Code REST Client extension.

Start the Spring Boot app first, then open `moviebookingapp.http` and send the
requests from top to bottom.

Before running the full flow:

- Set `@baseUrl` if your app is not running on `http://localhost:8080`.
- Set `@adminPassword` to your real admin password.
- Change `@runSuffix` when rerunning against the same database.
- Keep the 2027 dates or move them further into the future if needed.

## Files

- `moviebookingapp.http`: happy-path requests for every public, auth, admin, and booking endpoint.
- `error-cases.http`: useful validation, auth, forbidden, and conflict examples.
- `postman-yaak-test-data.json`: request bodies and variables you can copy into Postman or Yaak.

## Notes

All current HTTP endpoints can be exercised with REST Client.

These behaviors are not practical to test with REST Client alone:

- Invalid JWT secret startup failure: the app fails before HTTP requests are available.
- Missing admin password outside `dev`: the app fails before HTTP requests are available.
- Logout with a valid JWT missing `jti`: generated app tokens include `jti`, so this requires a separately signed JWT without that claim.
