package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.tie.ImportContext;
import org.tie.Tie;
import org.tie.TypedRecord;
import org.tie.config.FieldDefinition;
import org.tie.config.FileFormat;
import org.tie.config.IntegrationDefinition;
import org.tie.config.Layout;
import org.tie.config.LocalDirectoryChannel;
import org.tie.spi.Outcome;

class ExampleApplicationTest {

    private static ConfigurableApplicationContext start(Path directory) {
        return new SpringApplicationBuilder(ExampleApplication.class).web(WebApplicationType.NONE)
                .run("--tie.working-directory=" + directory, "--people.input-directory=" + directory);
    }

    @Test
    void aYamlDefinitionUsesTheTransformerBean(@TempDir Path directory) throws IOException {
        Files.writeString(directory.resolve("people_20261026.csv"), "nif;born\n12345678z;31/12/1990\n");

        try (ConfigurableApplicationContext context = start(directory)) {
            List<TypedRecord> people = context.getBean(PeopleImport.class).run();

            assertEquals(1, people.size());
            assertEquals("12345678Z", people.getFirst().get("taxId"));
            assertEquals(LocalDate.of(1990, 12, 31), people.getFirst().get("born"));
        }
    }

    @Test
    void theInjectedTieAlsoRunsJavaApiDefinitions(@TempDir Path directory) throws IOException {
        Path in = Files.createDirectories(directory.resolve("in"));
        Files.writeString(in.resolve("altas.csv"), "nif\n12345678z\n");
        List<Object> values = new ArrayList<>();

        try (ConfigurableApplicationContext context = start(directory)) {
            context.getBean(Tie.class).importFiles(IntegrationDefinition.importing("altas")
                    .inputChannel(LocalDirectoryChannel.of(in))
                    .layout(Layout.builder("altas").fileNamePattern("altas").allowedFormats(FileFormat.CSV)
                            .field(FieldDefinition.builder("nif").transformer("upperNif")))
                    .build(), ImportContext.builder(record -> {
                        values.add(record.get("nif"));
                        return Outcome.APPLIED;
                    }).build());
        }

        assertEquals(List.of("12345678Z"), values);
    }
}
