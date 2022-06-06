package domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class PowerReportTest {

    @Test
    void fillMissingRanges() {
        // Given
        PowerReport powerReport = buildHounslowPowerReport();
        powerReport.getPowerMetrics().remove(2);

        // When
        powerReport.fillMissingRanges();

        // Then
        Assertions.assertEquals(powerReport.getPowerMetrics().size(), 4);
        Assertions.assertEquals(powerReport.getPowerMetrics().get(2).power(), 741);
    }

    @Test
    void formatRangesForDuration() {
        // Given
        PowerReport powerReport = buildHounslowPowerReport();

        // When
        powerReport.formatRangesForDuration(Duration.ofMinutes(15));

        // Then
        Assertions.assertEquals(powerReport.getPowerMetrics().size(), 16);
    }

    @Test
    void mergePowerMetrics() {
        // Given
        PowerReport powerReport1 = buildHounslowPowerReport();
        PowerReport powerReport2 = buildFormattedBarnsleyPowerReport();

        // When
        PowerReport mergedPowerReport = PowerReport.mergePowerMetrics(List.of(powerReport1, powerReport2));

        // Then
        Assertions.assertEquals(mergedPowerReport.size(), 4);
        Assertions.assertEquals( 1346, mergedPowerReport.getPowerMetrics().get(0).power());
        Assertions.assertEquals(1554, mergedPowerReport.getPowerMetrics().get(1).power());
        Assertions.assertEquals( 799, mergedPowerReport.getPowerMetrics().get(2).power());
        Assertions.assertEquals( 1238, mergedPowerReport.getPowerMetrics().get(3).power());
    }

    private PowerReport buildHounslowPowerReport() {
        LocalDateTime startDateTime = LocalDateTime.parse("2022-06-06T12:15:00");
        Instant start = startDateTime.toInstant(ZoneOffset.UTC);
        PowerMetric powerMetric1 = new PowerMetric(start, start.plus(Duration.ofMinutes(60)), 756);
        PowerMetric powerMetric2 = new PowerMetric(start.plus(Duration.ofMinutes(60)), start.plus(Duration.ofMinutes(120)), 804);
        PowerMetric powerMetric3 = new PowerMetric(start.plus(Duration.ofMinutes(120)), start.plus(Duration.ofMinutes(180)), 575);
        PowerMetric powerMetric4 = new PowerMetric(start.plus(Duration.ofMinutes(180)), start.plus(Duration.ofMinutes(240)), 678);
        return new PowerReport(new ArrayList<>(List.of(powerMetric1, powerMetric2, powerMetric3, powerMetric4)));
    }

    private PowerReport buildFormattedBarnsleyPowerReport() {
        LocalDateTime startDateTime = LocalDateTime.parse("2022-06-06T12:15:00");
        Instant start = startDateTime.toInstant(ZoneOffset.UTC);
        PowerMetric powerMetric1 = new PowerMetric(start, start.plus(Duration.ofMinutes(60)), 590);
        PowerMetric powerMetric2 = new PowerMetric(start.plus(Duration.ofMinutes(60)), start.plus(Duration.ofMinutes(120)), 750);
        PowerMetric powerMetric3 = new PowerMetric(start.plus(Duration.ofMinutes(120)), start.plus(Duration.ofMinutes(180)), 224);
        PowerMetric powerMetric4 = new PowerMetric(start.plus(Duration.ofMinutes(180)), start.plus(Duration.ofMinutes(240)), 560);
        return new PowerReport(new ArrayList<>(List.of(powerMetric1, powerMetric2, powerMetric3, powerMetric4)));
    }
}
