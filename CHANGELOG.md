# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.2.0] — 2026-11-09

### Added

- `copyMode` on a field with `copyFrom` (`CopyMode.TEXT` or `CANONICAL`, also in YAML/JSON and the schema).
  With `CANONICAL`, an imported copy receives the canonical text of the source's typed value
  (`2026-12-31`, `1234.50`, `true`) whatever the source's `typeFormat`. When the source is not `STRING`
  and the copy's own steps leave that text unchanged, the target tries the canonical form before its
  `typeFormat`. `TEXT`, the default, keeps the 0.1.0 behaviour. Ignored on export. `copyMode` without
  `copyFrom` is a configuration error.

### Changed

- `FileHook` is now called for provided inputs too. With a hook registered, a provided input is spooled to
  the working directory and gets `onFileReceived` (null `timestamp`, `size` = bytes read) and
  `onFileCompleted`, like a channel file. Without a hook nothing changes.
- `FieldDefinition` has a new `copyMode` component after `copyFrom`, so its canonical constructor changed.
  The builder is unaffected.

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

[Unreleased]: https://github.com/tie-integration-engine/tie/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.2.0
[0.1.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.1.0
