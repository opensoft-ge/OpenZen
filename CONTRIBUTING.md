# Contributing to OpenZen

Contributions to code, tests, documentation, accessibility and reproducible bug reports are welcome. Read the README and capability or compatibility ledger before choosing work: implemented behavior and future plans are deliberately distinguished.

1. Open an issue to discuss a significant change or propose a focused fix.
2. Follow the README for the supported Java version, local setup and required tools. Work on a branch and use synthetic data.
3. Add meaningful acceptance coverage for changed behavior, preserve existing security boundaries and run `sh scripts/verify.sh`. Keep the exact coverage gates and existing checks intact.
4. Update the relevant documentation and submit a pull request explaining the behavior, validation and any remaining limits.

By intentionally submitting a contribution for inclusion, you agree to license your contribution under Apache License 2.0, consistent with section 5 of [LICENSE](LICENSE), unless explicitly stated otherwise. Only submit material you have the right to contribute. Preserve third-party attribution, notices and applicable licence terms.

Do not include credentials, private customer data or confidential source material. Report suspected security vulnerabilities privately to support@opensoft.ge with the project name and a safe reproduction; do not publish exploit details in a public issue before coordinated review.

No paid support or consulting engagement is required to contribute.

## AI product direction

Requirement added 2026-11-16: OpenZen must become fully AI enabled, agentic and conversational across its core journeys. The [AI capability and acceptance guide](docs/ai-capabilities.md) defines the required work and evidence. This direction does not change the implementation scope or historical validation results recorded above.
