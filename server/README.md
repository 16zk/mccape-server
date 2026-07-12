# Mc Cape development API

This server is separate from the mod JAR. It uses Javalin, H2 migrations, and
the same PNG validation logic as the client. The `X-Dev-Player` fake identity
provider works only when `APP_ENV=development` and
`ALLOW_INSECURE_DEV_AUTH=true`; startup rejects this combination in production.

Render free storage is ephemeral, so uploaded capes may disappear after a
restart. Use a persistent database/object store before calling this production.

Run tests/build only: `gradlew.bat :server:test :server:build`.
