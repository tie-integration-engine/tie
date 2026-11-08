package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tie.config.IntegrationDefinition;

class ImportPeopleTest {

    static Stream<Arguments> definitions() {
        return Stream.of(
                Arguments.of("Java API", (Function<Path, IntegrationDefinition>) PeopleDefinitions::javaApi),
                Arguments.of("YAML", (Function<Path, IntegrationDefinition>) PeopleDefinitions::yaml),
                Arguments.of("JSON", (Function<Path, IntegrationDefinition>) PeopleDefinitions::json));
    }

    @Test
    void theThreeConfigurationsAreTheSameDefinition(@TempDir Path directory) {
        IntegrationDefinition javaApi = PeopleDefinitions.javaApi(directory);

        assertEquals(javaApi, PeopleDefinitions.yaml(directory));
        assertEquals(javaApi, PeopleDefinitions.json(directory));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("definitions")
    void importsTypedRecords(String style, Function<Path, IntegrationDefinition> definition, @TempDir Path directory)
            throws IOException {
        Files.writeString(directory.resolve("people_20261026.csv"), "nif;born;sex\n12345678z;31/12/1990;F\n");

        assertEquals(List.of(Map.of("taxId", "12345678Z", "born", LocalDate.of(1990, 12, 31), "sex", "Female",
                "country", "ES")), ImportPeople.run(definition.apply(directory)));
    }
}
