package domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PowerReport {

    private List<PowerMetric> powerMetrics;

    public PowerReport(List<PowerMetric> powerMetrics) {
        this.powerMetrics = powerMetrics;
    }

    public void fillMissingRanges() {
        for (int i = 0; i < powerMetrics.size() - 1; i++) {
            Instant endCurrentMetric = powerMetrics.get(i).end();
            Instant startNextMetric = powerMetrics.get(i + 1).start();

            // If missing range found
            if (!endCurrentMetric.equals(startNextMetric)) {
                int powerCurrentMetric = powerMetrics.get(i).power();
                int powerNextMetric = powerMetrics.get(i + 1).power();
                // Compute medium power from N and N+1 metric
                int mediumPower = (powerCurrentMetric + powerNextMetric) / 2;
                PowerMetric powerMetric = new PowerMetric(endCurrentMetric, startNextMetric, mediumPower);
                // And add it to metrics
                powerMetrics.add(i + 1, powerMetric);
            }
        }
    }

    public void formatRangesForDuration(Duration newIntervalDuration) {
        List<PowerMetric> mutablePowerMetrics = new ArrayList<>(powerMetrics);

        // Get central metric interval (which is constant)
        Instant startFirstMetric = mutablePowerMetrics.get(0).start();
        Instant endFirstMetric = mutablePowerMetrics.get(0).end();
        Duration currentIntervalDuration = Duration.between(startFirstMetric, endFirstMetric);

        if (!currentIntervalDuration.equals(newIntervalDuration)) {
            int ratio = (int) (currentIntervalDuration.toMinutes() / newIntervalDuration.toMinutes());

            for (int i = 0; i < mutablePowerMetrics.size(); i = i + ratio) {
                int power = mutablePowerMetrics.get(i).power();
                int newPower = power / ratio;

                Instant start = mutablePowerMetrics.get(i).start();
                mutablePowerMetrics.remove(i);

                for (int x = i; x < i + ratio; x++) {
                    Instant newStart = start.plus(newIntervalDuration.multipliedBy(x - i));
                    Instant newEnd = newStart.plus(newIntervalDuration);
                    PowerMetric newPowerMetric = new PowerMetric(newStart, newEnd, newPower);
                    mutablePowerMetrics.add(x, newPowerMetric);
                }
            }
        }
        this.powerMetrics = Collections.unmodifiableList(mutablePowerMetrics);
    }

    public static PowerReport mergePowerMetrics(List<PowerReport> powerReports) {
        List<PowerMetric> mergedPowerMetrics = new LinkedList<>();

        for (int index = 0; index < powerReports.get(0).size(); index++) {
            List<PowerMetric> powerMetricForIndex = new LinkedList<>();
            for (PowerReport powerReport : powerReports) {
                powerMetricForIndex.add(powerReport.getPowerMetrics().get(index));
            }
            PowerMetric mergedPowerMetricForIndex = PowerMetric.merge(powerMetricForIndex);
            mergedPowerMetrics.add(mergedPowerMetricForIndex);
        }
        return new PowerReport(mergedPowerMetrics);
    }

    public int size() {
        return this.powerMetrics.size();
    }

    public List<PowerMetric> getPowerMetrics() {
        return powerMetrics;
    }
}
