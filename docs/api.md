# OpenZen API

All resource URLs end in `.json`, under `/api/v2`.
`GET|POST /tickets.json`, `GET|PUT|DELETE /tickets/{id}.json`, `GET /tickets/{id}/comments.json` serve agents.
Create with `{ticket: {subject, comment: {body, public?}, ...}}`; update with the same envelope. Fields include status, priority, tags and requester_id. Updates can require `{safe_update: true, updated_stamp: "..."}`; stale stamps fail with 409. Closed tickets cannot be edited.
`GET|POST /requests.json`, `GET|PUT /requests/{id}.json`, `GET /requests/{id}/comments.json` operate only on the current requester's own records. Create uses `{request: {subject, comment: {body}}}` and replies use `{request: {comment: {body}}}`. Requester identity cannot be overridden.
Numeric local identities are required. Editors/admins act as agents; end_user tokens cannot read the agent APIs. Viewers may use their own requester API but cannot modify agent tickets. Lists accept page and per_page (1–100). Private ticket comments never appear in request responses.

Requests require `Authorization: Bearer <token>`. `/healthz` is public. Unknown/repeated query parameters and unknown body fields are rejected. Authentication uses local TOKENS configuration, not vendor OAuth. Transactions commit only successful requests.

Ticket tags: `GET|PUT|POST|DELETE /api/v2/tickets/{id}/tags` (also `.json`). GET lists, PUT adds without losing existing tags, POST replaces, DELETE removes specified tags; every success returns 200 `{tags: [...]}`. Mutations accept required `tags` plus optional boolean `safe_update` and string `updated_stamp`. Editors/admins may mutate; viewers may read; end users cannot use this agent endpoint. Closed tickets and stale safe updates return 409. Inputs and resulting lists are limited to 100 tags of 1–100 nonblank characters; strings remain case-sensitive and are deduplicated on add/set. No normalization, user/organization tags, bulk updates or tag autocomplete. Transactions serialize concurrent additions.

## AI product direction

Requirement added 2026-11-16: OpenZen must become fully AI enabled, agentic and conversational across its core journeys. The [AI capability and acceptance guide](ai-capabilities.md) defines the required work and evidence. This direction does not change the implementation scope or historical validation results recorded above.
