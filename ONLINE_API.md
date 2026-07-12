# Mc Cape optional online API contract

The online mode is disabled by default. Local capes, editing, importing, and
rendering do not depend on this API. Production clients require HTTPS.

## Endpoints

- `POST /api/v1/capes`: authenticated PNG upload; maximum 5 MiB; validates the
  real PNG signature, decoded dimensions, decompression limits, and generated
  server-side filenames. Returns `201`, metadata JSON, `ETag`, and a cape ID.
- `GET /api/v1/players/{uuid}/cape`: returns `200` with a descriptor containing
  `playerId`, `capeId`, `textureUri`, `sha256`, `expiresAtEpochMillis`, and
  `etag`; returns `404` when no cape is shared.
- `DELETE /api/v1/players/{uuid}/cape`: authenticated owner-only removal;
  returns `204`.
- `GET /api/v1/capes/{id}/texture`: returns `image/png`, `ETag`,
  `Cache-Control`, and a bounded content length.

Errors use `{ "code": "stable_code", "message": "safe message" }` with
`400`, `401`, `403`, `404`, `413`, `415`, `429`, or `5xx` as appropriate.

Authentication must prove control of the Minecraft identity using short-lived
tokens. Microsoft passwords and tokens must never be collected or stored.
Servers must enforce rate limits, content validation, retention, moderation,
reporting, and deletion policies.
