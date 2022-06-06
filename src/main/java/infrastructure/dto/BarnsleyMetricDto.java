package infrastructure.dto;

import java.time.Instant;

public record BarnsleyMetricDto(Instant start_time, Instant end_time, int value) {
}
