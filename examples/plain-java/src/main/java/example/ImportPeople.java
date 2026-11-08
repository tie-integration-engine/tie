package example;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.tie.ImportContext;
import org.tie.ImportResult;
import org.tie.Tie;
import org.tie.config.IntegrationDefinition;
import org.tie.config.load.DefinitionLoader;
import org.tie.spi.Outcome;

/** Imports people_<date>.csv files from a directory, with the definition in YAML. */
public final class ImportPeople {

    static final String DEFINITION = """
            version: 1
            id: people
            direction: IMPORT
            inputChannels:
              - { type: LOCAL_DIRECTORY, directory: "%s" }
            layouts:
              - name: person
                fileNamePattern: people_{timestamp:yyyyMMdd}
                allowedFormats: [CSV]
                formatOptions: { csv: { separator: ";" } }
                fields:
                  - { name: taxId, column: nif, textTransforms: [UPPER] }
                  - { name: born, type: DATE, typeFormat: dd/MM/yyyy }
            """;

    private ImportPeople() {
    }

    /** Imports every candidate file and returns the typed records. */
    public static List<Map<String, Object>> run(Path directory) {
        IntegrationDefinition definition = DefinitionLoader.fromYaml(DEFINITION.formatted(directory));
        List<Map<String, Object>> people = new ArrayList<>();
        ImportResult result = Tie.builder().build().importFiles(definition, ImportContext.builder(record -> {
            people.add(Map.of("taxId", record.get("taxId"), "born", record.get("born")));
            return Outcome.APPLIED;
        }).build());
        if (result.error() != null || result.files().stream().anyMatch(file -> file.error() != null)) {
            throw new IllegalStateException("import failed: " + result);
        }
        return people;
    }

    public static void main(String[] args) {
        run(Path.of(args[0])).forEach(System.out::println);
    }
}
