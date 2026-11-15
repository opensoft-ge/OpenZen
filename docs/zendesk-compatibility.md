# OpenZen compatibility ledger

Baseline: empty directory, initialized as an independent repository. Guide: `../skills/open-zen/SKILL.md` and its bundled inventory. This iteration establishes an API foundation, not complete product parity.

Target: **Zendesk Support REST v2**, primary documentation accessed 2026-11-13: [reference](https://developer.zendesk.com/api-reference/ticketing/tickets/tickets/). Tests exercise our implementation; no licensed vendor conformance capture was available.

| ID | Capability | Implemented behavior | Boundary / next work |
|---|---|---|---|
| OZ-001 | Agent tickets | Create/list/read/update/delete, status/priority/tags | Delete requires local admin; no assignments or SLA engine |
| OZ-002 | Conversations | Append public/private comments; visibility persists | Text body only; no attachments/redaction |
| OZ-003 | Requester portal API | Own requests and public replies | Private comments and private descriptions stay hidden |
| OZ-004 | Safe updates | Persistent updated_stamp check; conflicting updates return 409 | Explicit opt-in safe_update, no vendor timestamp precision claim |
| OZ-005 | Paging | page/per_page and metadata | Offset pages; no cursor/incremental export |
| OZ-006 | Remaining product | Missing | Triggers, macros, routing, Guide, Talk, messaging, enterprise permissions, UI |

Cross-cutting foundation: persistent SQLite transactions, local bearer identity, tenant isolation, role checks, strict input validation, Docker deployment and exact coverage gates. Missing across the product: vendor OAuth/SSO, fine-grained ACLs, production migrations/backup tooling, distributed workers and vendor-identical UI. Coverage measures implemented Java, not the missing inventory.

Acceptance: valid lifecycle persists through app restart; invalid inputs fail without partial writes; tenant boundaries and roles hold; exact executable-line coverage includes every production Java class; branch counts are reported separately. Docker smoke exercises a real HTTP server. See [validation](validation.md).

## OZ-007 — ticket tag resource, 13 November 2026

Starting HEAD `cc44829`, main, clean tree. Implemented the missing tag resource from inventory A5.3, using the open-zen skill. Preferred over search (OZ-008, missing) and triggers (OZ-006, missing), since tags unblock routing while completing an existing stored field. [Official Support REST v2 tag reference](https://developer.zendesk.com/api-reference/ticketing/ticket-management/tags/), retrieved 2026-11-13, documents GET/PUT/POST/DELETE, tag envelopes, closed-ticket restrictions and safe updates. Local role mapping, tag limits and strict validation remain documented subset behavior; no live Zendesk conformance claim.

Implemented in `ApiController.java` with 15 new cases in `src/test/java/ge/opensoft/openzen/ContractTest.java` (all failed against missing baseline routes). Acceptance covers add/set/remove/list, duplicate and missing tags, empty lists, invalid payload rollback, combined-size limits, restart persistence, role and tenant boundaries, closed-ticket immutability, stale stamps and concurrent additions. Existing ticket reads see changes and timestamps advance. Current Java measurements and reproduction: [validation](validation.md).

## Java 25 migration

The runtime, HTTP acceptance harness and build now use Java 25 / Spring Boot 4.1.2. Existing persistence and implemented API behavior are retained. Current coverage and test results supersede earlier stack-specific measurements: see [validation](validation.md). This migration does not expand vendor-compatibility claims.

## Required product experience — UX-001

**Planned, before the first browser release.** Implement a professional, responsive interface with complete light/dark themes and a visible, keyboard-accessible persistent toggle. Follow the [product experience acceptance contract](product-experience.md). Dependencies: real product UI and the applicable identity/access foundations; validate both themes through actual workflows, including narrow screens, preference persistence, storage failure and accessible contrast/focus. The current API foundation does not provide this browser capability.
