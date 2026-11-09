# TIE

[![CI](https://github.com/tie-integration-engine/tie/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/tie-integration-engine/tie/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.tie-integration-engine/tie-core)](https://central.sonatype.com/artifact/io.github.tie-integration-engine/tie-core)

TIE is a stateless Java library for file integrations. It moves files between systems (SFTP, local
directories or streams you provide), reads and writes them as CSV, XLSX, XML or JSON, and turns every
value into a typed field through a declarative field engine. It stores nothing: configuration and
state come in, a typed record goes to your consumer, and a result with the new state comes out.

- **Import**: file → typed records handed to your `RecordConsumer`.
- **Export**: your records → a file delivered to a channel.
- **Streaming**: files are never loaded in memory, whatever their size.
- **No persistence**: watermarks, "already processed" checks and post-success moves are yours to keep.

Requires Java 25. Licensed under the MIT License.

## Modules

| Artifact | Contents |
|---|---|
| `io.github.tie-integration-engine:tie-core` | Model, field engine, formats, channels, JSON/YAML loading. No Spring. |
| `io.github.tie-integration-engine:tie-spring-boot-starter` | Optional Spring Boot 4 auto-configuration. |

```xml
<dependency>
    <groupId>io.github.tie-integration-engine</groupId>
    <artifactId>tie-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quick start

### With the Java API

```java
IntegrationDefinition definition = IntegrationDefinition.importing("people")
        .inputChannel(LocalDirectoryChannel.of(Path.of("/data/in")))
        .layout(Layout.builder("person")
                .fileNamePattern("people_{timestamp:yyyyMMdd}")
                .allowedFormats(FileFormat.CSV)
                .csv(CsvOptions.builder().separator(';').build())
                .field(FieldDefinition.builder("taxId").column("nif").textTransform(TextTransform.UPPER))
                .field(FieldDefinition.builder("born").type(FieldType.DATE).typeFormat(TypeFormat.date("dd/MM/yyyy"))))
        .duplicateProtection(new DuplicateProtection(true, PostSuccessAction.move("done")))
        .build();

Tie tie = Tie.builder().build();
ImportResult result = tie.importFiles(definition, ImportContext.builder(record -> {
            repository.save(record.get("taxId", String.class), record.get("born", LocalDate.class));
            return Outcome.APPLIED;
        })
        .watermarks(watermarkStore.load())
        .build());

watermarkStore.save(result.watermarks());   // TIE keeps no state: store the new watermarks yourself
```

### With a YAML document

```yaml
version: 1
id: people
direction: IMPORT
inputChannels:
  - type: SFTP
    host: sftp.example.com
    username: tie
    password: ${secret:sftp}          # resolved by your SecretProvider
    remoteDirectory: /in
    knownHostsFile: /etc/tie/known_hosts
duplicateProtection:
  watermark: true
  postSuccessAction: { type: MOVE, remoteFolder: done }
layouts:
  - name: person
    fileNamePattern: people_{timestamp:yyyyMMdd}
    allowedFormats: [CSV, XLSX]
    formatOptions:
      csv: { separator: ";" }
    fields:
      - { name: taxId, column: nif, textTransforms: [UPPER] }
      - { name: born, type: DATE, typeFormat: dd/MM/yyyy }
      - { name: bornIso, inInput: false, copyFrom: born, copyMode: CANONICAL }   # 2026-12-31
```

```java
IntegrationDefinition definition = DefinitionLoader.fromYaml(inputStream);
```

Unknown properties and invalid definitions throw a `ConfigurationException` listing every problem. The
JSON schema of the document is published in the jar as `/tie-definition.schema.json`.

### With Spring Boot

Add `tie-spring-boot-starter`. It creates a `Tie` bean unless you declare one, registers every
`FieldTransformer` bean under its bean name, and uses your `DefaultValueResolver`, `SecretProvider`
and `Clock` beans if present.

```yaml
tie:
  working-directory: /var/tie
```

## What happens to a file

| Situation | Result |
|---|---|
| Invalid configuration | `ConfigurationException` before any file is touched |
| A line with field errors, or rejected by the consumer | The line is rejected with every error (stable `code` and `args`); the file goes on |
| Rejections above `rejectionThreshold` | The file fails |
| Structural error in the middle of a file | `STREAMING`: earlier lines stay delivered. `VALIDATE_THEN_DELIVER`: nothing is delivered |
| A file fails | The run stops; later files are `NOT_PROCESSED`, nothing is moved and the watermark stays before it |
| The post-success move, delete or result report fails | Warning; the file still counts as applied |
| An export exceeds its threshold or is cancelled | Nothing is delivered |
| Delivery to a name that already exists | `onExistingFile`: `SUFFIX` (default), `FAIL` or `OVERWRITE` |

Deliveries are always written under a temporary `.<name>.part` name and renamed at the end, so a
partial file never appears under its final name.

## Things TIE does not do for you

- **Concurrency between runs**: two runs on the same source are not coordinated. Make sure only one runs
  at a time.
- **Persistence**: watermarks, processed-file registries and line history are yours to store.
- **Transactions**: TIE never undoes what your consumer applied. Use `FileHook.onFileCompleted` to
  commit per file before the file is moved and the watermark advances. The hook is also called for
  provided inputs, which are then spooled to the working directory first.

## Security

- SFTP host keys are always verified (`knownHostsFile` or `hostKeyFingerprint`); `insecureAcceptAnyHostKey`
  must be set explicitly and logs a warning on every connection.
- Credentials never appear in logs, exceptions or results; use `${secret:name}` with a `SecretProvider`.
- Record values are never logged above DEBUG.
- XML DTDs and external entities are rejected; XLSX files above POI's compression limits are rejected.
- Values that look like spreadsheet formulas are neutralized in CSV/XLSX result reports.

## Repositories

This repository holds the parent pom, the examples and the CI. The modules live in their own
repositories, included here as git submodules:

| Module | Repository |
|---|---|
| `tie-core` | [tie-integration-engine/tie-core](https://github.com/tie-integration-engine/tie-core) |
| `tie-spring-boot-starter` | [tie-integration-engine/tie-spring-boot-starter](https://github.com/tie-integration-engine/tie-spring-boot-starter) |

Clone with the submodules:

```shell
git clone --recurse-submodules https://github.com/tie-integration-engine/tie.git
# or, in an existing clone:
git submodule update --init
```

A change to a module is committed and pushed in that module's repository first; then this repository
records the new module commit:

```shell
cd tie-core && git switch main && git commit -am "…" && git push && cd ..
git add tie-core && git commit -m "Update tie-core" && git push
```

## Building

```shell
./mvnw verify               # unit and integration tests (embedded SFTP server)
./mvnw -P release verify    # also the slow tests (1 GB streams), sources and javadoc
```

The `examples/` directory has two applications built against the installed artifacts (`./mvnw install`
first):

- `examples/plain-java`: the same import configured with the Java API, a YAML document and a JSON
  document; a test checks that the three give equal definitions and the same records.
- `examples/spring-boot`: a service that loads its definition from a YAML file on the classpath and uses
  a `FieldTransformer` bean through the auto-configured `Tie`.
