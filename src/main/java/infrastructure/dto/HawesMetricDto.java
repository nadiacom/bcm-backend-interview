package infrastructure.dto;

import java.time.Instant;

public record HawesMetricDto(Instant start, Instant end, int power) {
}