import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.PowerMetric;
import domain.PowerReport;

import java.util.function.Function;

public enum OutputFormat {
    JSON(OutputFormat::jsonSerializer),
    CSV(OutputFormat::csvSerializer),
    ;

    private final Function<PowerReport, String> serializer;

    OutputFormat(Function<PowerReport, String> serializer) {
        this.serializer = serializer;
    }

    public static OutputFormat fromLowerCase(String outputFormat) {
        return OutputFormat.valueOf(outputFormat.toUpperCase());
    }

    public String serialize(PowerReport powerReport) {
        return serializer.apply(powerReport);
    }

    private static String jsonSerializer(PowerReport powerReport) {
        ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        try {
            return objectMapper.writeValueAsString(powerReport);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String csvSerializer(PowerReport powerReport) {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module());
        CsvSchema csvSchema = csvMapper
                .typedSchemaFor(PowerMetric.class)
                .withHeader()
                .withColumnSeparator(',');
        try {
            return csvMapper.writer(csvSchema).writeValueAsString(powerReport.getPowerMetrics());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
