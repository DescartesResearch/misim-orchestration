package cambio.simulator.orchestration.export.reporters;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.models.MiSimOrchestrationModel;
import cambio.simulator.orchestration.util.FileOps;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static cambio.simulator.orchestration.export.reporters.EventsReporter.generateEventsReport;
import static cambio.simulator.orchestration.export.reporters.KubernetesReporter.generateKubernetesReports;
import static cambio.simulator.orchestration.export.reporters.MiSimReporter.generateMiSimScalingReports;
import static cambio.simulator.orchestration.export.reporters.PerformanceReporter.generatePerformanceReport;

public class MainReporter {
    final static String ORCHESTRATION_MAIN_REPORTS_DIR = "orchestration_reports_new";
    final static String MISIM_STANDARD_SUBDIR = "misim_standard";
    final static String MISIM_SCALING_SUBDIR = "misim_scaling";
    final static String PERFORMANCE_SUBDIR = "performance";
    final static String ORCHESTRATION_SUBDIR = "orchestration";
    final static String PERFORMANCE_RESULTS_FILE = "performance_results.csv";
    final static String EVENTS_RESULTS_FILE = "events_results.csv";

    public static void generateReports(String currentRunName, MiSimModel model) {
        Path workingDirectoryPath = FileSystems.getDefault().getPath("").toAbsolutePath();
        Path orchestrationMainReportsPath = workingDirectoryPath.resolve(ORCHESTRATION_MAIN_REPORTS_DIR);
        Path runSpecificReportsPath = orchestrationMainReportsPath.resolve(currentRunName);

        Path miSimReportsPath = runSpecificReportsPath.resolve(MISIM_STANDARD_SUBDIR);
        Path miSimScalingPath = runSpecificReportsPath.resolve(MISIM_SCALING_SUBDIR);
        Path performancePath = runSpecificReportsPath.resolve(PERFORMANCE_SUBDIR);
        Path orchestrationPath = runSpecificReportsPath.resolve(ORCHESTRATION_SUBDIR);

        try {
            FileOps.copyDirectory(model.getExperimentMetaData().getReportLocation(), miSimReportsPath);
        } catch (IOException e) {
            System.err.println("Could not copy MiSim reports to report directory.\n" + e.getMessage());
        }

        generatePerformanceReport(model, performancePath.resolve(PERFORMANCE_RESULTS_FILE));
        generateEventsReport(performancePath.resolve(EVENTS_RESULTS_FILE));

        boolean isOrchestrationRun =
                model instanceof MiSimOrchestrationModel && ((MiSimOrchestrationModel) model).getOrchestrationConfig().isOrchestrate();
        if (isOrchestrationRun) {
            generateKubernetesReports(orchestrationPath);
        }
        generateMiSimScalingReports(miSimScalingPath);
    }


}
