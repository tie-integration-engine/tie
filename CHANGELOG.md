# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.5.0] — 2026-11-09

### Added

- `DefinitionWriter.toJson` / `toYaml`: an `IntegrationDefinition` built with the Java API (or loaded) written as
  the JSON or YAML document that `DefinitionLoader` reads back. The definition is validated first; keys follow the
  schema order, defaults are left out, so equal definitions give the same text. Literal credentials (`password`,
  `privateKey`, `privateKeyPassphrase` that are not a `${secret:name}` reference) are written as `***`.
- Programmable fields: `expression` on a field computes its final value. On import it runs in a second pass, in
  declaration order, and sees the field's value, the other fields, the parent record (`parent`, `parent.parent`),
  `today` and the context variables; the result is converted to the field's type and validated. On export it runs
  over the supplied values before the export steps. New SPI `ExpressionEngine` (`Tie.Builder.expressionEngine`),
  new line error code `EXPRESSION_FAILED`.
- New module `tie-jexl`: `JexlExpressionEngine`, with Apache Commons JEXL in a restricted sandbox (no class access,
  loops, `new` or side effects; misspelt names are errors) and the functions `tie:age`, `tie:years`, `tie:blank`,
  `tie:date`, `tie:format` and `tie:coalesce`.
- The Spring Boot starter registers a `JexlExpressionEngine` when `tie-jexl` is on the classpath and passes any
  `ExpressionEngine` bean to the `Tie` bean.

### Changed

- `FieldDefinition` has a new `expression` component after `defaultMode`, so its canonical constructor changed.
  The builder is unaffected.
- A credential equal to `***` (what `DefinitionWriter` writes for a literal one) is now a configuration problem.
- `copyFrom` of a field that has an `expression` is a configuration problem: the copy would get the value before
  the expression.

## [0.4.0] — 2026-11-09

### Added

- XML imports without `recordElement`: every child of the root element is a record, whatever its name (text,
  comments and processing instructions under the root are ignored). Exports still require `recordElement`.
- `json.firstArray`: with no `recordProperty`, the records are the root array or, for a root object, the first
  top-level property whose value is an array; earlier properties are skipped without being read. It cannot be
  combined with `recordProperty` on import and is ignored on export. `JsonOptions` gets a builder.
- A root import layout may omit `fileNamePattern` when every input channel is a `ProvidedStreamChannel`; a
  declared pattern is still validated.

### Changed

- `JsonOptions` has a new `firstArray` record component, so its canonical constructor changed; use
  `JsonOptions.builder()`. `JsonOptions.DEFAULTS` keeps the same values.

## [0.3.0] — 2026-11-09

### Added

- `defaultMode` on a field with `defaultValue` (`DefaultMode.CONVERT` or `TEXT`, also in YAML/JSON and the
  schema). With `TEXT`, the resolved default is validated but not converted, and reaches the consumer as a
  `String` whatever the field's type: an unresolved `${…}` template no longer rejects the line. `CONVERT`, the
  default, keeps the 0.2.0 behaviour. Ignored on export.
- XLSX reading options `skipBlankRecords` (as in CSV: a row whose cells are all blank is dropped and uses no
  line number), `cellValues` (`XlsxCellValues.CANONICAL`, the default, or `FORMATTED`: the text Excel
  displays per the cell's number format, e.g. `08001`, `25%`, `15/03/2024`) and `locale` (BCP 47, default
  `Locale.ROOT`) for `FORMATTED`. `XlsxOptions` gets a builder like `CsvOptions`.
- `TypeFormat.date(pattern, locale)` and `typeFormat: { pattern, locale }` in documents: month and day names
  and AM/PM of that locale (e.g. `31-dic-2026` with `es-ES`), when reading and writing.

### Changed

- `XlsxOptions` has three new record components (`skipBlankRecords`, `cellValues`, `locale`), so its
  canonical constructor changed; use `XlsxOptions.builder()`. `XlsxOptions.DEFAULTS` keeps the same values.
- `TypeFormat.DateFormat` has a new `locale` component: its canonical constructor and record patterns
  (`DateFormat(String pattern)`) no longer compile. `TypeFormat.date(pattern)` is unchanged.
- `RecordView.get(field)` is no longer always of the field's Java type: a `defaultMode: TEXT` field that took
  its default holds a `String`, and `get(field, LocalDate.class)` throws `ClassCastException` for it.
- `FieldDefinition` has a new `defaultMode` component after `defaultValue`. The builder is unaffected.

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

[Unreleased]: https://github.com/tie-integration-engine/tie/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.5.0
[0.4.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.4.0
[0.3.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.3.0
[0.2.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.2.0
[0.1.0]: https://github.com/tie-integration-engine/tie/releases/tag/v0.1.0
