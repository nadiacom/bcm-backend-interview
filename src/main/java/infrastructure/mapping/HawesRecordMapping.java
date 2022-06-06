package infrastructure.mapping;

import domain.PowerMetric;
import domain.PowerReport;
import infrastructure.dto.HawesMetricDto;

import java.util.List;

public class HawesRecordMapping {

    public static PowerReport from(List<HawesMetricDto> dtos) {
        List<PowerMetric> powerMetrics = dtos.stream()
                .map(HawesRecordMapping::from).toList();
        return new PowerReport(powerMetrics);
    }

    private static PowerMetric from(HawesMetricDto dto) {
        return new PowerMetric(dto.start(), dto.end(), dto.power());
    }
}
