package example;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.tie.FileResult;
import org.tie.ImportContext;
import org.tie.ImportResult;
import org.tie.Tie;
import org.tie.config.IntegrationDefinition;
import org.tie.spi.Outcome;

/**
 * Runs the people import with any of its definitions.
 *
 * <pre>java example.ImportPeople java|yaml|json /data/in</pre>
 */
public final class ImportPeople {

    private ImportPeople() {
    }

    /** Imports every candidate file and returns the typed records. */
    public static List<Map<String, Object>> run(IntegrationDefinition definition) {
        List<Map<String, Object>> people = new ArrayList<>();
        ImportResult result = Tie.builder().build().importFiles(definition, ImportContext.builder(record -> {
            Map<String, Object> person = new LinkedHashMap<>();
            for (String field : List.of("taxId", "born", "sex", "country")) {
                person.put(field, record.get(field));
            }
            people.add(person);
            return Outcome.APPLIED;
        }).build());
        if (result.error() != null) {
            throw new IllegalStateException("import failed: " + result.error().message());
        }
        for (FileResult file : result.files()) {
            if (file.error() != null) {
                throw new IllegalStateException(file.name() + " failed: " + file.error().message());
            }
        }
        return people;
    }

    public static void main(String[] args) {
        Path directory = Path.of(args[1]);
        IntegrationDefinition definition = switch (args[0]) {
            case "java" -> PeopleDefinitions.javaApi(directory);
            case "yaml" -> PeopleDefinitions.yaml(directory);
            case "json" -> PeopleDefinitions.json(directory);
            default -> throw new IllegalArgumentException("expected java, yaml or json, got " + args[0]);
        };
        run(definition).forEach(System.out::println);
    }
}
