# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [0.1.0] — 2026-11-08

First release.

### Added

- Imports from SFTP, local directories and provided streams; exports and `deliverFile` to SFTP, local
  directories and provided streams.
- CSV, XLSX, XML and JSON readers and writers, all incremental.
- Typed field engine: column location by position, header or letter; copies, trimming, text
  transforms, translations, custom transformers, type conversion, validations and empty-value handling.
- Nested child layouts in JSON and XML.
- Delivery strategies `STREAMING` and `VALIDATE_THEN_DELIVER`, rejection thresholds, consumer error
  policies, cancellation, run listener and file hooks.
- Stateless duplicate protection: watermarks, an already-processed predicate and post-success move or
  delete.
- Result reports in CSV, XLSX, JSON and XML.
- Declarative definitions in JSON or YAML with a published JSON schema.
- Stable catalogs of line error codes, file error types and warning codes.
- Spring Boot 4 starter.

[0.1.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.1.0
