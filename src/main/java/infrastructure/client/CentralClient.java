package infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import domain.Central;
import domain.PowerReportProvider;
import domain.PowerReport;
import infrastructure.dto.BarnsleyMetricDto;
import infrastructure.dto.HawesMetricDto;
import infrastructure.dto.HounslowMetricDto;
import infrastructure.mapping.BarnsleyRecordMapping;
import infrastructure.mapping.HawesRecordMapping;
import infrastructure.mapping.HounslowRecordMapping;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CentralClient implements PowerReportProvider {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    private static final String BARNSLEY_URL = "https://interview.beta.bcmenergy.fr/barnsley";
    private static final String HAWES_URL = "https://interview.beta.bcmenergy.fr/hawes";
    private static final String HOUNSLOW_URL = "https://interview.beta.bcmenergy.fr/hounslow";

    @Override
    public PowerReport get(LocalDate from, LocalDate to, Central central) throws URISyntaxException, ExecutionException, InterruptedException {

        return switch (central) {
            case HAWES -> getHawesReport(from, to);
            case BARNSLEY -> getBarnsleyReport(from, to);
            case HOUNSLOW -> getHounslowReport(from, to);
        };
    }

    private PowerReport getHawesReport(LocalDate from, LocalDate to) throws URISyntaxException, ExecutionException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(buildURI(from, to, HAWES_URL))
                .header("Accept", "application/json")
                .build();

        List<HawesMetricDto> hawesRecordDto = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        return objectMapper.readValue(json, new TypeReference<List<HawesMetricDto>>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .get();

        return HawesRecordMapping.from(hawesRecordDto);
    }

    private PowerReport getBarnsleyReport(LocalDate from, LocalDate to) throws URISyntaxException, ExecutionException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(buildURI(from, to, BARNSLEY_URL))
                .header("Accept", "application/json")
                .build();

        List<BarnsleyMetricDto> barnsleyReportDto = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    try {
                        return  objectMapper.readValue(json, new TypeReference<List<BarnsleyMetricDto>>() {
                        });
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .get();

        return BarnsleyRecordMapping.from(barnsleyReportDto);
    }

    private PowerReport getHounslowReport(LocalDate from, LocalDate to) throws URISyntaxException, ExecutionException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(buildURI(from, to, HOUNSLOW_URL))
                .header("Accept", "application/csv")
                .build();

        List<HounslowMetricDto> hounslowRecordDto = HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(csv -> {
                    try {
                        CsvMapper csvMapper = new CsvMapper();
                        csvMapper.registerModule(new JavaTimeModule())
                                .registerModule(new Jdk8Module());
                        CsvSchema csvSchema = csvMapper
                                .typedSchemaFor(HounslowMetricDto.class)
                                .withHeader()
                                .withColumnSeparator(',');

                        MappingIterator<HounslowMetricDto> complexUsersIter = csvMapper
                                .readerWithTypedSchemaFor(HounslowMetricDto.class)
                                .with(csvSchema)
                                .readValues(csv);

                        return complexUsersIter.readAll();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .get();

        return HounslowRecordMapping.from(hounslowRecordDto);
    }

    private URI buildURI(LocalDate from, LocalDate to, String url) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        uriBuilder.addParameter("from", from.format(DATE_TIME_FORMATTER));
        uriBuilder.addParameter("to", to.format(DATE_TIME_FORMATTER));
        return uriBuilder.build();
    }
}
