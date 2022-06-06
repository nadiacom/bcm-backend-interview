package domain;

import java.time.Instant;
import java.util.List;

public record PowerMetric(Instant start, Instant end, int power) {

    public static PowerMetric merge(List<PowerMetric> powerMetrics) {
        Instant start = powerMetrics.get(0).start();
        Instant end = powerMetrics.get(0).end();
        int sumPower = 0;

        for (int i = 0; i < powerMetrics.size(); i++) {
            sumPower += powerMetrics.get(i).power();
        }
        return new PowerMetric(start, end, sumPower);
    }
}
