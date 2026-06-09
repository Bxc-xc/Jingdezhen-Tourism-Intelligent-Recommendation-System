Ingestion Pipeline (Standalone)
--------------------------------

Purpose
- Collect real-world POI data (scenic, food merchants) from open APIs.
- Normalize/clean into your site's schema.
- Output preview (dry-run) JSON/CSV for manual review.
- Optionally call Admin Import API to persist after approval.

Key Principles
- Standalone (no imports from business code).
- Config-driven (YAML/JSON).
- Default dry-run; safe and reversible.
- Auditable: logs, batch IDs, and result summaries.

Structure
- config/: environment configs (dev/staging/prod).
- src/: Python entrypoints and modules.
- output/: preview artifacts (JSON/CSV) + logs.

Run
- Dry-run:
  - python -m src.main --config config/dev.yaml --dry-run
- Generate CSV previews for review:
  - python -m src.main --config config/dev.yaml --dry-run --preview csv
- After approval, import:
  - python -m src.main --config config/prod.yaml --import --batch-id YYYYMMDD-001

Notes
- Do not commit secrets. Use .env or CI/CD secrets.
- Respect source terms and rate limits.

