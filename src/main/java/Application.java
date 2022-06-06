import domain.PowerReport;
import domain.GenerateFullReportService;
import infrastructure.client.CentralClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Application {

    public static void main(String[] args) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate from = LocalDate.parse(args[0], formatter);
        LocalDate to = LocalDate.parse(args[1], formatter);
        OutputFormat outputFormat = OutputFormat.fromLowerCase(args[2]);

        CentralClient centralClient = new CentralClient();
        GenerateFullReportService generateFullReportService = new GenerateFullReportService(centralClient);

        PowerReport powerReport = generateFullReportService.generateReport(from, to);
        System.out.println(outputFormat.serialize(powerReport));
    }
}
