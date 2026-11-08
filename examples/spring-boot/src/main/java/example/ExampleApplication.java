package example;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.tie.spi.FieldTransformer;

/** The starter creates the Tie bean; this bean becomes the transformer "upperNif". */
@SpringBootApplication
public class ExampleApplication {

    @Bean
    FieldTransformer upperNif() {
        return (value, record, direction) -> value.toUpperCase(Locale.ROOT);
    }

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
