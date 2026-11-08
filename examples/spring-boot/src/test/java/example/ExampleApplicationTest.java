package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.tie.ImportContext;
import org.tie.Tie;
import org.tie.config.FieldDefinition;
import org.tie.config.FileFormat;
import org.tie.config.IntegrationDefinition;
import org.tie.config.Layout;
import org.tie.config.LocalDirectoryChannel;
import org.tie.spi.Outcome;

class ExampleApplicationTest {

    @Test
    void theStarterProvidesATieWithTheTransformerBean(@TempDir Path directory) throws IOException {
        Path in = Files.createDirectories(directory.resolve("in"));
        Files.writeString(in.resolve("altas.csv"), "nif\n12345678z\n");
        List<Object> values = new ArrayList<>();

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(ExampleApplication.class)
                .web(WebApplicationType.NONE).run("--tie.working-directory=" + directory)) {
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
