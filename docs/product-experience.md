# OpenZen product experience

Status reviewed 2026-11-15: Planned — this release is an API foundation with no browser interface. Professional light/dark UI is required before its first browser release; no browser dark mode is advertised.

## Required product experience

- Use a restrained professional palette, clear typography, consistent spacing, predictable navigation and coherent controls. Default direction: blue-grey surfaces, steel-blue actions, slate text and semantic status colours. Preserve product-specific workflows; do not copy another product's proprietary visual assets.
- Provide complete light and dark themes using semantic colour tokens. Cover public login/invitation/recovery pages as well as authenticated navigation, lists, forms, dialogs, reports, charts, empty/error/loading states and admin screens when present. Do not simulate dark mode with an inversion filter.
- Put a clearly labelled, keyboard-operable theme toggle in a consistent, easily found position, including on mobile. Expose its state to assistive technology. Use the system preference initially; persist an explicit choice across navigation and reloads without a server round trip. Handle unavailable browser storage gracefully, preserve the current-page choice and keep the app usable without JavaScript. State whether preferences are device-local or account-synchronized.
- Meet WCAG AA contrast targets in both themes: at least 4.5:1 for normal text, 3:1 for large text and meaningful control/focus indicators. Do not convey status solely by colour. Respect reduced motion; keep focus visible, controls labelled and content usable on narrow screens and at zoom.
- Qualify real workflows in both themes with browser assertions, keyboard checks, automated accessibility scans and visual inspection at desktop/mobile sizes. Check first load, OS preference changes, manual override, navigation, reload and storage failures. Apply existing exact executable-code coverage gates to theme behaviour; screenshots and passing contrast scans alone do not establish full accessibility conformance.

Sources: [W3C text contrast](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html), [non-text contrast](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html) and [visible focus](https://www.w3.org/WAI/WCAG22/Understanding/focus-visible.html), reviewed 2026-11-15. The palette and interaction requirements are OpenSoft product decisions, not vendor parity claims.

## Delivery tracking

Experience ID: `UX-001`. Track implementation and remaining work in the [capability ledger](zendesk-compatibility.md). For a first UI release, connect real authenticated product workflows, implement both themes and qualify the above journeys. A design requirement does not change the implemented API/CLI contract.
