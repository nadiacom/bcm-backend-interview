package infrastructure.dto;

import java.time.Instant;

public record HounslowMetricDto(Instant debut, Instant fin, int valeur) {

}