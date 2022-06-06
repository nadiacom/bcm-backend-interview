package infrastructure.mapping;

import domain.PowerMetric;
import domain.PowerReport;
import infrastructure.dto.HounslowMetricDto;

import java.util.List;

public class HounslowRecordMapping {

    public static PowerReport from(List<HounslowMetricDto> dtos) {
        List<PowerMetric> powerMetrics = dtos.stream()
                .map(HounslowRecordMapping::from).toList();
        return new PowerReport(powerMetrics);
    }

    private static PowerMetric from(HounslowMetricDto dto) {
        return new PowerMetric(dto.debut(), dto.fin(), dto.valeur());
    }
}
