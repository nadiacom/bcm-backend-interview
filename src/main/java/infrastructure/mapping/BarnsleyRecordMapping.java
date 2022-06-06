package infrastructure.mapping;

import domain.PowerMetric;
import domain.PowerReport;
import infrastructure.dto.BarnsleyMetricDto;

import java.util.List;

public class BarnsleyRecordMapping {

    public static PowerReport from(List<BarnsleyMetricDto> dtos) {
        List<PowerMetric> powerMetrics = dtos.stream()
                .map(BarnsleyRecordMapping::from).toList();
        return new PowerReport(powerMetrics);
    }

    private static PowerMetric from(BarnsleyMetricDto dto) {
        return new PowerMetric(dto.start_time(), dto.end_time(), dto.value());
    }
}
