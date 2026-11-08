package example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tie.ImportContext;
import org.tie.ImportResult;
import org.tie.Tie;
import org.tie.TypedRecord;
import org.tie.config.IntegrationDefinition;
import org.tie.config.load.DefinitionLoader;
import org.tie.spi.Outcome;

/** The people import, with its definition in {@code integrations/people.yaml} and the auto-configured Tie. */
@Service
public class PeopleImport {

    private final Tie tie;
    private final IntegrationDefinition definition;

    public PeopleImport(Tie tie, @Value("classpath:integrations/people.yaml") Resource yaml,
                        @Value("${people.input-directory}") Path inputDirectory) throws IOException {
        this.tie = tie;
        try (InputStream in = yaml.getInputStream()) {
            String document = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("@INPUT_DIRECTORY@", inputDirectory.toString().replace("\\", "\\\\"));
            this.definition = DefinitionLoader.fromYaml(document);
        }
    }

    /** Imports every candidate file and returns the records the consumer received. */
    public List<TypedRecord> run() {
        List<TypedRecord> people = new ArrayList<>();
        ImportResult result = tie.importFiles(definition, ImportContext.builder(record -> {
            people.add(record);
            return Outcome.APPLIED;
        }).build());
        if (result.error() != null || result.files().stream().anyMatch(file -> file.error() != null)) {
            throw new IllegalStateException("import failed: " + result);
        }
        return people;
    }
}
