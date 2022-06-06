package domain;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public interface PowerReportProvider {

    PowerReport get(LocalDate from, LocalDate to, Central central) throws URISyntaxException, ExecutionException, InterruptedException;
}
