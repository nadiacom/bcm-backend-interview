package domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateFullReportService {

    private final PowerReportProvider provider;
    private static final Duration METRIC_INTERVAL = Duration.ofMinutes(15);

    public GenerateFullReportService(PowerReportProvider provider) {
        this.provider = provider;
    }

    public PowerReport generateReport(LocalDate from, LocalDate to) {
        List<PowerReport> reports = Arrays.stream(Central.values())
                .map(central -> {
                    try {
                        return provider.get(from, to, central);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .peek(PowerReport::fillMissingRanges)
                .peek(powerReport ->  powerReport.formatRangesForDuration(METRIC_INTERVAL))
                .collect(Collectors.toList());
        return PowerReport.mergePowerMetrics(reports);
    }
}
