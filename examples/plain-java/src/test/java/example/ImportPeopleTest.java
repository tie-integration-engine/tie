package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImportPeopleTest {

    @Test
    void importsTypedRecords(@TempDir Path directory) throws IOException {
        Files.writeString(directory.resolve("people_20261026.csv"), "nif;born\n12345678z;31/12/1990\n");

        assertEquals(List.of(Map.of("taxId", "12345678Z", "born", LocalDate.of(1990, 12, 31))),
                ImportPeople.run(directory));
    }
}
