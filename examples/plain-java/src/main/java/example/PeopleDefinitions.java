package example;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.tie.config.CsvOptions;
import org.tie.config.FieldDefinition;
import org.tie.config.FieldType;
import org.tie.config.FileFormat;
import org.tie.config.IntegrationDefinition;
import org.tie.config.Layout;
import org.tie.config.LocalDirectoryChannel;
import org.tie.config.TextTransform;
import org.tie.config.TypeFormat;
import org.tie.config.load.DefinitionLoader;

/** The same import of people_<date>.csv files, configured three ways. */
public final class PeopleDefinitions {

    private static final String DIRECTORY_PLACEHOLDER = "@INPUT_DIRECTORY@";

    private PeopleDefinitions() {
    }

    /** With the Java API. */
    public static IntegrationDefinition javaApi(Path inputDirectory) {
        return IntegrationDefinition.importing("people")
                .inputChannel(LocalDirectoryChannel.of(inputDirectory))
                .layout(Layout.builder("person")
                        .fileNamePattern("people_{timestamp:yyyyMMdd}")
                        .allowedFormats(FileFormat.CSV)
                        .csv(CsvOptions.builder().separator(';').build())
                        .field(FieldDefinition.builder("taxId").column("nif").textTransform(TextTransform.UPPER))
                        .field(FieldDefinition.builder("born").type(FieldType.DATE)
                                .typeFormat(TypeFormat.date("dd/MM/yyyy")))
                        .field(FieldDefinition.builder("sex").translation("M", "Male").translation("F", "Female"))
                        .field(FieldDefinition.builder("country").inInput(false).defaultValue("ES")))
                .build();
    }

    /** From the YAML document {@code people.yaml} on the classpath. */
    public static IntegrationDefinition yaml(Path inputDirectory) {
        return DefinitionLoader.fromYaml(document("people.yaml", inputDirectory));
    }

    /** From the JSON document {@code people.json} on the classpath. */
    public static IntegrationDefinition json(Path inputDirectory) {
        return DefinitionLoader.fromJson(document("people.json", inputDirectory));
    }

    /** Reads a document and puts the input directory in it (escaped for JSON and quoted YAML strings). */
    private static String document(String resource, Path inputDirectory) {
        try (InputStream in = PeopleDefinitions.class.getResourceAsStream("/" + resource)) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return text.replace(DIRECTORY_PLACEHOLDER, inputDirectory.toString().replace("\\", "\\\\"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
