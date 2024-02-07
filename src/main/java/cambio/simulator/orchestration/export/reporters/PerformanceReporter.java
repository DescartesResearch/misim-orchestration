package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.export.CSVBuilder;
import cambio.simulator.orchestration.util.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class PerformanceReporter {
    public static void generatePerformanceReport(MiSimModel model, Path csvPath) {

        CSVBuilder csvBuilder = new CSVBuilder();
        csvBuilder.headers(Arrays.asList("full_duration_ms", "experiment_duration_ms", "report_duration_ms"));

        ArrayList<String> content = new ArrayList<>();
        ExperimentMetaData metaData = model.getExperimentMetaData();
        content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getExecutionDuration())));
        content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getExperimentExecutionDuration())));
        content.add(String.valueOf(Util.nanoSecondsToMilliSeconds(metaData.getSetupExecutionDuration())));


        csvBuilder.row(content);
        try {
            csvBuilder.build(csvPath);
        } catch (CSVBuilder.CSVBuilderException e) {
            System.err.println(e.getMessage());
        }

    }

}
